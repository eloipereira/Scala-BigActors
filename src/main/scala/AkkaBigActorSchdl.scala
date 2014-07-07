package bigactors.akkaBigActors

import akka.actor.{ActorRef, Actor}
import akka.event.Logging
import akka.util.{Timeout}
import bigactors._
import edu.berkeley.eloi.bigraph.{Bigraph, Place}
import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.concurrent.Await
import scala.concurrent.duration._
import akka.pattern.ask


class AkkaBigActorSchdl(bigraphManager: ActorRef) extends Actor {
  import context._

  val logging = Logging(context.system, this)
  private implicit val hostRelation = new HashMap[ActorRef,Symbol]

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
        hostRelation += requester -> hostId
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
      QueryInterpreter.AkkaEvaluate(query, hostId.name, bigraph, hostRelation) match {
        case Left(b) => {
          logging.info("[BigActorSchdl]:\t Observed Bigraph: " + b)
          requester ! b
        }
        case Right(a) => {
          logging.info("[BigActorSchdl]:\t Observed BigActors: " + a)
          requester ! a
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
        System.err.println("[BigActorSchdl]:\t Host " + hostId.name + "is not included on redex/reactum of "+ brr)
        System.exit(0)
      }
    }
    case SEND_REQUEST_AKKA(msg,rcv,hostId) => {
      logging.info("[BigActorSchdl]:\t got a snd request from " + sender)
      val bigraph = requestBigraph
      val senderHost = bigraph.getNode(hostId.name)
      val destHost = bigraph.getNode(hostRelation(rcv).name)
      if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
        logging.info("[BigActorSchdl]:\t Hosts " + hostId.name + " and " +  hostRelation(rcv).name + " are connected.")
        rcv ! msg
      } else {
        logging.error("[BigActorSchdl]:\t Hosts " + hostId.name + " and " +  hostRelation(rcv).name + " are not connected.")
        System.exit(0)
      }
    }
    case MIGRATION_REQUEST_AKKA(newHostId,hostId) => {
      logging.info("[BigActorSchdl]:\t got a mgrt request from " + hostId + " to " +newHostId)
      val bigraph = requestBigraph
      val currentHost = bigraph.getNode(hostId.name)
      val destHost = bigraph.getNode(newHostId.name)
      if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
        logging.info("[BigActorSchdl]:\t Hosts connected. Migrating...")
        hostRelation += sender -> newHostId
      } else {
        System.err.println("[BigActorSchdl]:\t Hosts " + hostId + " and " + newHostId + " are not connected.")
        System.exit(0)
      }
    }
    case REQUEST_HOSTING_RELATION =>{
      sender ! hostRelation
    }
    case _ => println("[BigActorSchdl]:\t UNKNOWN REQUEST")
  }
}
