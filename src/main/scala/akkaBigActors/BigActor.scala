package akkaBigActors

import akka.actor.{Actor, ActorRef}
import akka.event.Logging
import akka.pattern.ask
import akka.util.Timeout
import bigactors._
import edu.berkeley.eloi.bigraph.{BRR, Place}

import scala.concurrent.Await
import scala.concurrent.duration._

abstract class BigActor(var hostID: Symbol, bigActorSchdl: ActorRef) extends Actor {
  val logging = Logging(context.system, this)

  hostBigActor

  def hostBigActor = {
    implicit val timeout = Timeout(5 seconds)
    val future = bigActorSchdl ? HOSTING_REQUEST_AKKA(hostID, self)
    Await.result(future, timeout.duration) match {
      case HOSTING_SUCCESSFUL => {
        logging.info("[BigActor]:\t\t\t BigActor " + this + " successfully hosted at " + hostID)
      }
      case HOSTING_UNSUCCESSFUL => logging.error("[BigActor]:\t\t\t BigActor " + this + " failed to be hosted at " + hostID + ". Make sure hust exists.")
    }
  }

  def observe(query: Query) = {
    bigActorSchdl ! OBSERVATION_REQUEST_AKKA(query,hostID)
  }
  def control(brr: BRR){
    bigActorSchdl ! CONTROL_REQUEST_AKKA(brr,hostID)
  }
  def migrate(newHostId: Symbol){
    bigActorSchdl ! MIGRATION_REQUEST_AKKA(newHostId,hostID)
  }
  def sendMsg(msg: Any,rcv: ActorRef){
    bigActorSchdl ! SEND_REQUEST_AKKA(msg,rcv,hostID)
  }

  def observeAndWaitForBigraph(query: Query): Array[Place]= {
    implicit val timeout = Timeout(5 seconds)
    val future = bigActorSchdl ? OBSERVATION_REQUEST_AKKA(query,hostID)
    Await.result(future,timeout.duration).asInstanceOf[Array[Place]]
  }

  def observeAndWaitForBigActors(query: Query): Array[ActorRef] = {
    implicit val timeout = Timeout(5 seconds)
    val future = bigActorSchdl ? OBSERVATION_REQUEST_AKKA(query,hostID)
    Await.result(future,timeout.duration).asInstanceOf[Array[ActorRef]]
  }

  def HOST = observeAndWaitForBigraph(Host)
  def PARENT_HOST = observeAndWaitForBigraph(Parent(Host))
  def CHILDREN_PARENT_HOST = observeAndWaitForBigraph(Children(Parent(Host)))
  def LINKED_TO_HOST = observeAndWaitForBigraph(Linked_to(Host))
  def HOSTED_AT_LINKED_TO_HOST  = observeAndWaitForBigActors(Hosted_at(Linked_to(Host)))
  def MOVE_HOST_TO(loc: Place) = control(new BRR(PARENT_HOST.head.toBgm + ".( $0 |" + HOST.head.toBgm + ")||" +  loc.toBgm + ".($1) ->" + PARENT_HOST.head.toBgm + ".( $0 ) || " + loc.toBgm +".($1|"+HOST.head.toBgm + ")"))
  // TODO - create CONNECT_HOST_TO(link: String)

}

