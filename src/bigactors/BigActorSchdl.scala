package bigactors

import scala.actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug
import scala.collection.mutable.HashMap

sealed trait BigActorSchdlAPI
case class HOSTING_REQUEST(bigActor: BigActor, hostId: Symbol) extends BigActorSchdlAPI
case class OBSERVATION_REQUEST(query: String, bigActorID: Symbol) extends BigActorSchdlAPI
case class CONTROL_REQUEST(brr: BRR, bigActorID: Symbol) extends BigActorSchdlAPI
case class SEND_REQUEST(msg: Message, bigActorID: Symbol) extends BigActorSchdlAPI
case class MIGRATION_REQUEST(newHostId: Symbol, bigActorID: Symbol) extends BigActorSchdlAPI
case class SEND_SUCCESSFUL(msg: Message)
case class OBSERVATION_RESULT(obs: Observation)


object BigActorSchdl extends Actor{
  var debug = false

  private val hostRelation = new HashMap[Symbol,Symbol]
  private val addressesRelation = new HashMap[Symbol,BigActor]

  start

  def act() {
    loop {
      react{
        case HOSTING_REQUEST(bigActor: BigActor, hostId: Symbol) =>{
          BigraphManager ! GET_BIGRAPH
          receive{
            case bigraph: Bigraph => {
              if (bigraph.getPlaces.contains(new Node(hostId.name))) {
                Debug.println("Hosting BigActor at host " + hostId,debug)
                hostRelation += bigActor.bigActorID -> hostId
                addressesRelation +=  bigActor.bigActorID -> bigActor
                bigActor.start()
              }
              else {
                System.err.println("BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
                System.exit(0)
              }

            }
          }
        }
        case OBSERVATION_REQUEST(query, bigActorID) => {
          Debug.println("got a obs request with query " + query + " from "+bigActorID,debug)
          BigraphManager ! GET_BIGRAPH
          receive{
            case bigraph: Bigraph => {
              val host = bigraph.getNode(hostRelation(bigActorID).name)
              val obs = new Observation(SimpleQueryCompiler.generate(query,host,bigraph))
              Debug.println("Observation: "+obs,debug)
              addressesRelation(bigActorID) ! OBSERVATION_RESULT(obs)
            }
          }
        }
        case CONTROL_REQUEST(brr, bigActorID) => {
          Thread.sleep(3000)
          Debug.println("got a ctr request " + brr,debug)
          BigraphManager ! GET_BIGRAPH
          receive{
            case bigraph: Bigraph => {
              if (brr.getRedex.getNodes.contains(bigraph.getNode(hostRelation(bigActorID).name))
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostRelation(bigActorID).name))){
                BigraphManager ! EXECUTE_BRR(brr)
              } else {
                System.err.println("Host " + hostRelation(bigActorID) + "is not included on redex/reactum of "+ brr)
                System.exit(0)
              }
            }
          }
        }
        case SEND_REQUEST(msg, bigActorID) => {
          val senderID = bigActorID
          val receiverID = msg.receiverID
          Debug.println("got a snd request from " + bigActorID,debug)
          BigraphManager ! GET_BIGRAPH
          receive{
            case bigraph: Bigraph => {
              val senderHost = bigraph.getNode(hostRelation(senderID).name)
              val destHost = bigraph.getNode(hostRelation(receiverID).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are connected.",debug)
                addressesRelation(receiverID) ! SEND_SUCCESSFUL(msg)
              } else {
                System.err.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case MIGRATION_REQUEST(newHostId, bigActorID) => {
          Debug.println("got a mgrt request from " + hostRelation(bigActorID) + " to " +newHostId,debug)
          BigraphManager ! GET_BIGRAPH
          receive{
            case bigraph: Bigraph => {
              val currentHost = bigraph.getNode(hostRelation(bigActorID).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts connected. Migrating...",debug)
                hostRelation += bigActorID -> newHostId
              } else {
                System.err.println("Hosts " + hostRelation(bigActorID) + " and " + newHostId + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case _ => println("UNKNOWN REQUEST")
      }
    }
  }
}
