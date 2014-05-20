package bigactors

import scala.actors.{OutputChannel, Actor}
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug
import scala.collection.mutable.HashMap


object BigActorSchdl extends Actor {
  var debug = true
  private val hostRelation = new HashMap[OutputChannel[Any],Symbol]

  def act() {
    loop {
      react{
        case HOSTING_REQUEST(hostId) =>{
          Debug.println("[BigActorSchdl]:\t got a host request from " + sender + " to be hosted at "+hostId,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.map(p=>p.getId).contains(hostId.name)) {
                Debug.println("[BigActorSchdl]:\t Hosting BigActor at host " + hostId,debug)
                hostRelation += requester -> hostId
                requester ! HOSTING_SUCCESSFUL
              }
              else {
                System.err.println("[BigActorSchdl]:\t BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
                System.exit(0)
              }
            }
          }
        }
        case OBSERVATION_REQUEST(query) => {
          Debug.println("[BigActorSchdl]:\t got a obs request with query " + query + " from "+sender,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val hostId: String = hostRelation(requester).name
              val obs = new Observation(SimpleQueryCompiler.generate(query,hostId,bigraph))
              Debug.println("[BigActorSchdl]:\t Observation: "+obs,debug)
              requester ! obs
            }
          }
        }
        case CONTROL_REQUEST(brr) => {
          Debug.println("[BigActorSchdl]:\t got a ctr request " + brr,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (brr.getRedex.getNodes.contains(bigraph.getNode(hostRelation(requester).name))
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostRelation(requester).name))){
                BigraphManager ! EXECUTE_BRR(brr)
              } else {
                System.err.println("[BigActorSchdl]:\t Host " + hostRelation(requester) + "is not included on redex/reactum of "+ brr)
                System.exit(0)
              }
            }
          }
        }
        case SEND_REQUEST(msg,rcv) => {
          Debug.println("[BigActorSchdl]:\t got a snd request from " + sender,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostRelation(requester).name)
              val destHost = bigraph.getNode(hostRelation(rcv).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("[BigActorSchdl]:\t Hosts " + hostRelation(requester).name + " and " +  hostRelation(rcv).name + " are connected.",debug)
                rcv ! msg
              } else {
                System.err.println("[BigActorSchdl]:\t Hosts " + hostRelation(requester).name + " and " +  hostRelation(rcv).name + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case MIGRATION_REQUEST(newHostId) => {
          Debug.println("[BigActorSchdl]:\t got a mgrt request from " + hostRelation(sender) + " to " +newHostId,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val currentHost = bigraph.getNode(hostRelation(requester).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("[BigActorSchdl]:\t Hosts connected. Migrating...",debug)
                hostRelation += sender -> newHostId
              } else {
                System.err.println("[BigActorSchdl]:\t Hosts " + hostRelation(requester) + " and " + newHostId + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case _ => println("[BigActorSchdl]:\t UNKNOWN REQUEST")
      }
    }
  }
  start
}
