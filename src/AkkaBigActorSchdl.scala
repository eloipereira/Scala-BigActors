package bigactors.akka

import _root_.akka.actor.{ActorRef, Actor}
import _root_.akka.event.Logging
import bigactors._
import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, HashMap}


class AkkaBigActorSchdl(bigraphManager: ActorRef) extends Actor {
  import context._

  val logging = Logging(context.system, this)
  private val hostRelation = new HashMap[ActorRef,Symbol]


  def receive = {
    case HOSTING_REQUEST_AKKA(hostId,actorRef) =>{
      logging.info("[BigActorSchdl]:\t got a host request from " + actorRef + " to be hosted at "+hostId)
      bigraphManager ! BIGRAPH_REQUEST
      val requester = sender
      become({
        case BIGRAPH_RESPONSE(bigraph) => {
          if (bigraph.getPlaces.map(p=>p.getId).contains(hostId.name)) {
            logging.info("[BigActorSchdl]:\t Hosting BigActor at host " + hostId)
            hostRelation += actorRef -> hostId
            requester ! HOSTING_SUCCESSFUL
            unbecome()
          }
          else {
            logging.error("[BigActorSchdl]:\t BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
            System.exit(0)
          }
        }
      }, discardOld = false)
    }
    case OBSERVATION_REQUEST(query) => {
      logging.info("[BigActorSchdl]:\t got a obs request with query " + query + " from " + sender)
      val requester = sender
      bigraphManager ! BIGRAPH_REQUEST
      become({
        case BIGRAPH_RESPONSE(bigraph) => {
          val hostId: String = hostRelation(requester).name
          val result = QueryInterpreter.AkkaEvaluate(query, hostId, bigraph, hostRelation)
          result match {
            case Left(b) => {
              logging.info("[BigActorSchdl]:\t Observed Bigraph: " + b)
              requester ! b
            }
            case Right(a) => {
              logging.info("[BigActorSchdl]:\t Observed BigActors: " + a)
              requester ! a
            }
          }
          unbecome()
        }
      }, discardOld = false)
    }
    case CONTROL_REQUEST(brr) => {
      logging.info("[BigActorSchdl]:\t got a ctr request " + brr)
      val requester = sender
      bigraphManager ! BIGRAPH_REQUEST
      become({
        case BIGRAPH_RESPONSE(bigraph) => {
          if (brr.getRedex.getNodes.contains(bigraph.getNode(hostRelation(requester).name))
            || brr.getReactum.getNodes.contains(bigraph.getNode(hostRelation(requester).name))){
            bigraphManager ! EXECUTE_BRR(brr)
          } else {
            System.err.println("[BigActorSchdl]:\t Host " + hostRelation(requester) + "is not included on redex/reactum of "+ brr)
            System.exit(0)
          }
          unbecome()
        }
      },discardOld = false)
    }
    case SEND_REQUEST_AKKA(msg,rcv) => {
      logging.info("[BigActorSchdl]:\t got a snd request from " + sender)
      val requester = sender
      bigraphManager ! BIGRAPH_REQUEST
      become({
        case BIGRAPH_RESPONSE(bigraph) => {
          val senderHost = bigraph.getNode(hostRelation(requester).name)
          val destHost = bigraph.getNode(hostRelation(rcv).name)
          if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
            logging.info("[BigActorSchdl]:\t Hosts " + hostRelation(requester).name + " and " +  hostRelation(rcv).name + " are connected.")
            rcv ! msg
          } else {
            logging.error("[BigActorSchdl]:\t Hosts " + hostRelation(requester).name + " and " +  hostRelation(rcv).name + " are not connected.")
            System.exit(0)
          }
          unbecome()
        }
      },discardOld = false)
    }
    case MIGRATION_REQUEST(newHostId) => {
      logging.info("[BigActorSchdl]:\t got a mgrt request from " + hostRelation(sender) + " to " +newHostId)
      val requester = sender
      bigraphManager ! BIGRAPH_REQUEST
      become({
        case BIGRAPH_RESPONSE(bigraph) => {
          val currentHost = bigraph.getNode(hostRelation(requester).name)
          val destHost = bigraph.getNode(newHostId.name)
          if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
            logging.info("[BigActorSchdl]:\t Hosts connected. Migrating...")
            hostRelation += sender -> newHostId
          } else {
            System.err.println("[BigActorSchdl]:\t Hosts " + hostRelation(requester) + " and " + newHostId + " are not connected.")
            System.exit(0)
          }
          unbecome()
        }
      },discardOld = false)
    }
    case REQUEST_HOSTING_RELATION =>{
      sender ! hostRelation
    }
    case _ => println("[BigActorSchdl]:\t UNKNOWN REQUEST")
  }
}
