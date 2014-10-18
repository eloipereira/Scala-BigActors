package bigactors

import java.io.FileInputStream
import java.util.Properties

import org.apache.commons.logging.{Log, LogFactory}

import scala.actors.{Actor, OutputChannel}
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap



object BigActorSchdl extends Actor {
  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))

  private val log: Log = LogFactory.getLog("BigActorSchdl")


  implicit private val hostingRelation = new HashMap[OutputChannel[Any],Symbol]
  def act() {
    loop {
      react{
        case HOSTING_REQUEST(hostId) =>{
          log.debug("Got a host request from " + sender + " to be hosted at "+hostId)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.map(p=>p.getId).contains(hostId.name)) {
                log.debug("Hosting BigActor at host " + hostId)
                hostingRelation += requester -> hostId
                requester ! HOSTING_SUCCESSFUL
              }
              else {
                log.error("BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
                System.exit(0)
              }
            }
          }
        }
        case OBSERVATION_REQUEST(query) => {
          log.debug("Got a obs request with query " + query + " from " + sender)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive {
            case BIGRAPH_RESPONSE(bigraph) => {
              implicit val hostId: Symbol = hostingRelation(requester)
              val result = QueryInterpreter.evaluate(query,hostId,bigraph,hostingRelation)
              result match {
                case Left(b) => {
                  log.debug("Observed Bigraph: " + b)
                  requester ! b
                }
                case Right(a) => {
                  log.debug("Observed BigActors: " + a)
                  requester ! a
                }
              }
            }
          }
        }
        case CONTROL_REQUEST(brr) => {
          log.debug("Got a ctr request " + brr)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (brr.getRedex.getNodes.map(n=> n.getId).contains(bigraph.getNode(hostingRelation(requester).name).getId)
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostingRelation(requester).name))){
                BigraphManager ! EXECUTE_BRR(brr)
              } else {
                BigraphManager ! EXECUTE_BRR(brr)
                log.warn("Host " + hostingRelation(requester).name + " is not included on redex/reactum of "+ brr)
              }
            }
          }
        }
        case SEND_REQUEST(msg,rcv) => {
          log.debug("Got a snd request from " + sender)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostingRelation(requester).name)
              val destHost = bigraph.getNode(hostingRelation(rcv).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                log.debug("Hosts " + hostingRelation(requester).name + " and " +  hostingRelation(rcv).name + " are connected.")
                rcv ! msg
              } else {
                log.warn("Hosts " + hostingRelation(requester).name + " and " +  hostingRelation(rcv).name + " are not connected.")
              }
            }
          }
        }
        case MIGRATION_REQUEST(newHostId) => {
          log.debug("Got a mgrt request from " + hostingRelation(sender) + " to " +newHostId)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val currentHost = bigraph.getNode(hostingRelation(requester).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                log.debug("Hosts connected. Migrating...")
                hostingRelation += sender -> newHostId
              } else {
                log.warn("Hosts " + hostingRelation(requester) + " and " + newHostId + " are not connected.")
              }
            }
          }
        }
        case REQUEST_HOSTING_RELATION =>{
          sender ! hostingRelation
        }
        case _ => log.warn("Unknown request.")
      }
    }
  }
  start
}
