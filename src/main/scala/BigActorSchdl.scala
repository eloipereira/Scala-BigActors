package bigactors

import java.io.FileInputStream
import java.util.Properties

import edu.berkeley.eloi.bigraph.Bigraph
import edu.berkeley.eloi.concreteBgm2Java.Debug

import scala.actors.{Actor, OutputChannel}
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap



object BigActorSchdl extends Actor {
  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val debug = prop.getProperty("debug").toBoolean

  implicit private val hostingRelation = new HashMap[OutputChannel[Any],Symbol]
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
                hostingRelation += requester -> hostId
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
          Debug.println("[BigActorSchdl]:\t got a obs request with query " + query + " from " + sender, debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive {
            case BIGRAPH_RESPONSE(bigraph) => {
              implicit val hostId: Symbol = hostingRelation(requester)
              val result = QueryInterpreter.evaluate(query,hostId,bigraph,hostingRelation)
              result match {
                case Left(b) => {
                  Debug.println("[BigActorSchdl]:\t Observed Bigraph: " + b, debug)
                  requester ! b
                }
                case Right(a) => {
                  Debug.println("[BigActorSchdl]:\t Observed BigActors: " + a, debug)
                  requester ! a
                }
              }
            }
          }
        }
        case CONTROL_REQUEST(brr) => {
          Debug.println("[BigActorSchdl]:\t got a ctr request " + brr,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (brr.getRedex.getNodes.contains(bigraph.getNode(hostingRelation(requester).name))
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostingRelation(requester).name))){
                BigraphManager ! EXECUTE_BRR(brr)
              } else {
                System.err.println("[BigActorSchdl]:\t Host " + hostingRelation(requester) + "is not included on redex/reactum of "+ brr)
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
              val senderHost = bigraph.getNode(hostingRelation(requester).name)
              val destHost = bigraph.getNode(hostingRelation(rcv).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("[BigActorSchdl]:\t Hosts " + hostingRelation(requester).name + " and " +  hostingRelation(rcv).name + " are connected.",debug)
                rcv ! msg
              } else {
                System.err.println("[BigActorSchdl]:\t Hosts " + hostingRelation(requester).name + " and " +  hostingRelation(rcv).name + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case MIGRATION_REQUEST(newHostId) => {
          Debug.println("[BigActorSchdl]:\t got a mgrt request from " + hostingRelation(sender) + " to " +newHostId,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val currentHost = bigraph.getNode(hostingRelation(requester).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("[BigActorSchdl]:\t Hosts connected. Migrating...",debug)
                hostingRelation += sender -> newHostId
              } else {
                System.err.println("[BigActorSchdl]:\t Hosts " + hostingRelation(requester) + " and " + newHostId + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case REQUEST_HOSTING_RELATION =>{
          sender ! hostingRelation
        }
        case _ => println("[BigActorSchdl]:\t UNKNOWN REQUEST")
      }
    }
  }
  start
}
