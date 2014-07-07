package bigactors.akkaBigActors

/**
 * Created by eloi on 03-07-2014.
 */

import java.nio.file.Paths

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import bigactors.BigActor._
import bigactors.{Host, Parent, RENDEZVOUS_AT_LOCATION}
import edu.berkeley.eloi.bigraph.Place

object ExampleAkkaRendezvous0 extends App {
  implicit val system = ActorSystem("mySystem")

  val bigraphManager = system.actorOf(Props(classOf[AkkaBigraphManager], Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm").toString, true, false))
  val bigraphScheduler = system.actorOf(Props(classOf[AkkaBigActorSchdl], bigraphManager))

  val r0BA = system.actorOf(Props(classOf[Leader], 'r0))
  val r1BA = system.actorOf(Props(classOf[Follower], 'r1))
  val r2BA = system.actorOf(Props(classOf[Follower], 'r2))
  val r3BA = system.actorOf(Props(classOf[Follower], 'r3))
  val r4BA = system.actorOf(Props(classOf[Follower], 'r4))

  r0BA ! "start"

  class Leader(host: Symbol) extends AkkaBigActor(host,bigraphScheduler){
    def receive = {
      case "start" =>
        val rvLoc = PARENT_HOST.head
        r1BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
        r2BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
        r3BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
        r4BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
    }
  }

  class Follower(host: Symbol) extends AkkaBigActor(host,bigraphScheduler){
    def receive = {
      case RENDEZVOUS_AT_LOCATION(loc) => {
        MOVE_HOST_TO(loc)
      }
    }
  }

}
