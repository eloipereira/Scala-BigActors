package bigactors.akkaBigActors

/**
 * Created by eloi on 03-07-2014.
 */

import akka.actor.{ActorSystem, Props}
import bigactors.RENDEZVOUS_AT_LOCATION

object ExampleAkkaROSRendezvous0 extends App {

  // launch ROSBigraphSimulator first

  implicit val system = ActorSystem("mySystem")
  val bigraphManager = system.actorOf(Props(classOf[ROSBigraphManager], "localhost", 11311))
  val bigraphScheduler = system.actorOf(Props(classOf[BigActorSchdl], bigraphManager))

  val r0BA = system.actorOf(Props(classOf[Leader], 'r0))
  val r1BA = system.actorOf(Props(classOf[Follower], 'r1))
  val r2BA = system.actorOf(Props(classOf[Follower], 'r2))
  val r3BA = system.actorOf(Props(classOf[Follower], 'r3))
  val r4BA = system.actorOf(Props(classOf[Follower], 'r4))

  r0BA ! "start"

  class Leader(host: Symbol) extends BigActor(host,bigraphScheduler){
    def receive = {
      case "start" =>
        val rvLoc = PARENT_HOST.head
        r1BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
        r2BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
        r3BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
        r4BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
    }
  }

  class Follower(host: Symbol) extends BigActor(host,bigraphScheduler){
    def receive = {
      case RENDEZVOUS_AT_LOCATION(loc) => {
        MOVE_HOST_TO(loc)
      }
    }
  }

}
