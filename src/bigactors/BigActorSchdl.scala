package bigactors

import scala.actors.Actor
import scala.actors.remote._
import scala.actors.remote.RemoteActor._
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug
import scala.collection.mutable.HashMap
import scala.actors.Actor._
import bigactors.EXECUTE_BRR
import bigactors.CONTROL_REQUEST
import bigactors.OBSERVATION_REQUEST
import bigactors.MIGRATION_REQUEST
import scala.actors.remote.Node
import bigactors.HOSTING_REQUEST
import bigactors.SEND_REQUEST


sealed trait BigActorSchdlAPI
case class HOSTING_REQUEST(bigActor: BigActor, hostId: Symbol) extends BigActorSchdlAPI
case class OBSERVATION_REQUEST(query: String, bigActorID: Symbol) extends BigActorSchdlAPI
case class CONTROL_REQUEST(brr: BRR, bigActorID: Symbol) extends BigActorSchdlAPI
case class SEND_REQUEST(msg: Message, bigActorID: Symbol) extends BigActorSchdlAPI
case class MIGRATION_REQUEST(newHostId: Symbol, bigActorID: Symbol) extends BigActorSchdlAPI


object BigActorSchdl extends Actor{
  var debug = true

  private val hostRelation = new HashMap[Symbol,Symbol]
  private val addressesRelation = new HashMap[Symbol,BigActor]

  start
  def act() {
    alive(9010)
    register('bigActorSchdl, self)

    val bigraphManager = select(Node("localhost",9010), 'bigraphManager)

    loop {
      react{
        case HOSTING_REQUEST(bigActor: BigActor, hostId: Symbol) =>{
          bigraphManager ! GET_BIGRAPH
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.contains(new BigraphNode(hostId.name))) {
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
          bigraphManager ! GET_BIGRAPH
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val host = bigraph.getNode(hostRelation(bigActorID).name)
              val obs = new Observation(SimpleQueryCompiler.generate(query,host,bigraph))
              Debug.println("Observation: "+obs,debug)
              val c = select(Node("localhost",9010), bigActorID)
              c ! obs
            }
          }
        }
        case CONTROL_REQUEST(brr, bigActorID) => {
          Debug.println("got a ctr request " + brr,debug)
          bigraphManager ! GET_BIGRAPH
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (brr.getRedex.getNodes.contains(bigraph.getNode(hostRelation(bigActorID).name))
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostRelation(bigActorID).name))){
                bigraphManager ! EXECUTE_BRR(brr)
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
          bigraphManager ! GET_BIGRAPH
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostRelation(senderID).name)
              val destHost = bigraph.getNode(hostRelation(receiverID).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are connected.",debug)
                val c = select(Node("localhost",9010), receiverID)
                c ! msg.message
              } else {
                System.err.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case MIGRATION_REQUEST(newHostId, bigActorID) => {
          Debug.println("got a mgrt request from " + hostRelation(bigActorID) + " to " +newHostId,debug)
          bigraphManager ! GET_BIGRAPH
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
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
