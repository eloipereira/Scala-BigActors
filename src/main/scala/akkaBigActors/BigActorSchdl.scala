package akkaBigActors

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import bigactors._
import edu.berkeley.eloi.bigraph.Bigraph

import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap
import scala.concurrent.Await
import scala.concurrent.duration._


class BigActorSchdl(bigraphManager: ActorRef) extends Actor {

  val logging = Logging(context.system, this)
  private implicit val hostingRelation = new HashMap[ActorRef,Symbol]

  def requestBigraph: Bigraph = {
    implicit val timeout = Timeout(5 seconds)
    val futureBigraph = bigraphManager ? BIGRAPH_REQUEST
    Await.result(futureBigraph, timeout.duration) match {
      case BIGRAPH_RESPONSE(bigraph) => bigraph
    }
  }


  def receive = {
    case HOSTING_REQUEST_AKKA(hostId,actorRef) =>{
      logging.info("[BigActorSchdl]:\t got a host request from " + actorRef + " to be hosted at "+hostId)
      val requester = sender
      val bigraph = requestBigraph
      if (bigraph.getPlaces.map(p => p.getId).contains(hostId.name)) {
        logging.info("[BigActorSchdl]:\t Hosting BigActor at host " + hostId)
        hostingRelation += actorRef -> hostId
        requester ! HOSTING_SUCCESSFUL
      }
      else {
        logging.error("[BigActorSchdl]:\t BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
        System.exit(0)
      }
    }
    case OBSERVATION_REQUEST_AKKA(query,hostId) => {
      logging.info("[BigActorSchdl]:\t got a obs request with query " + query + " from " + sender)
      val requester = sender
      val bigraph = requestBigraph
      QueryInterpreter.evaluate(query,hostId,bigraph,hostingRelation) match {
        case Left(b) => {
          logging.info("[BigActorSchdl]:\t Observed Bigraph: " + b)
          requester ! b
        }
        case Right(as) => {
          logging.info("[BigActorSchdl]:\t Observed BigActors: " + as.foldRight(" ")((b:ActorRef,a: String)=> a + b.toString ))
          requester ! as
        }
      }
    }
    case CONTROL_REQUEST_AKKA(brr,hostId) => {
      logging.info("[BigActorSchdl]:\t got a ctr request " + brr)
      val bigraph = requestBigraph
      if (brr.getRedex.getNodes.contains(bigraph.getNode(hostId.name))
        || brr.getReactum.getNodes.contains(bigraph.getNode(hostId.name))){
        bigraphManager ! EXECUTE_BRR(brr)
      } else {
        logging.error("[BigActorSchdl]:\t Host " + hostId.name + "is not included on redex/reactum of "+ brr)
      }
    }
    case SEND_REQUEST_AKKA(msg,rcv,hostId) => {
      logging.info("[BigActorSchdl]:\t got a snd request from " + sender)
      val bigraph = requestBigraph
      val senderHost = bigraph.getNode(hostId.name)
      val destHost = bigraph.getNode(hostingRelation(rcv).name)
      if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
        logging.info("[BigActorSchdl]:\t Hosts " + hostId.name + " and " +  hostingRelation(rcv).name + " are connected.")
        rcv ! msg
      } else {
        logging.error("[BigActorSchdl]:\t Hosts " + hostId.name + " and " +  hostingRelation(rcv).name + " are not connected.")
      }
    }
    case MIGRATION_REQUEST_AKKA(newHostId,hostId) => {
      logging.info("[BigActorSchdl]:\t got a mgrt request from " + hostId + " to " +newHostId)
      val bigraph = requestBigraph
      val currentHost = bigraph.getNode(hostId.name)
      val destHost = bigraph.getNode(newHostId.name)
      if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
        logging.info("[BigActorSchdl]:\t Hosts connected. Migrating...")
        hostingRelation += sender -> newHostId
      } else {
        logging.error("[BigActorSchdl]:\t Hosts " + hostId + " and " + newHostId + " are not connected.")
      }
    }
    case REQUEST_HOSTING_RELATION =>{
      sender ! hostingRelation
    }
    case _ => println("[BigActorSchdl]:\t UNKNOWN REQUEST")
  }
}
