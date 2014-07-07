/**
 * Created by eloi on 03-07-2014.
 */

package bigactors.akkaBigActors

import java.nio.file.Paths

import akka.actor.{ActorSystem, Props}
import bigactors.RENDEZVOUS_AT_LOCATION
import bigactors.akkaBigActors.{AkkaBigActor, AkkaBigActorSchdl, AkkaBigraphManager}

object ExampleAkkaRendezvous1 extends App {
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
        val bigactors = HOSTED_AT_LINKED_TO_HOST
        bigactors.foreach{b=>
          b ! RENDEZVOUS_AT_LOCATION(rvLoc)
        }
      case _ => println("unknown message")
    }
  }

  class Follower(host: Symbol) extends AkkaBigActor(host,bigraphScheduler){

    def receive = {
      case RENDEZVOUS_AT_LOCATION(loc) => {
        MOVE_HOST_TO(loc)
      }
      case _ => println("unknown message")
    }
  }

}
