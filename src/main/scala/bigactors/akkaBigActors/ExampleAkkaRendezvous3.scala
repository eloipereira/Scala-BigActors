/**
 * Created by eloi on 03-07-2014.
 */

package bigactors.akkaBigActors

import java.nio.file.Paths

import akka.actor.{ActorRef, ActorSystem, Props}
import edu.berkeley.eloi.bigraph.Place

object ExampleAkkaRendezvous3 extends App {
  implicit val system = ActorSystem("mySystem")

  val bigraphManager = system.actorOf(Props(classOf[BigMCBigraphManager], Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm").toString, true, false))
  val bigraphScheduler = system.actorOf(Props(classOf[AkkaBigActorSchdl], bigraphManager))

  system.actorOf(Props(classOf[AgreeAndPursue], 'r0))
  system.actorOf(Props(classOf[AgreeAndPursue], 'r1))
  system.actorOf(Props(classOf[AgreeAndPursue], 'r2))
  system.actorOf(Props(classOf[AgreeAndPursue], 'r3))
  system.actorOf(Props(classOf[AgreeAndPursue], 'r4))

  class AgreeAndPursue(host: Symbol) extends AkkaBigActor(host,bigraphScheduler){
    var leader: ActorRef = self
    var rvLoc: Place = PARENT_HOST.head
    broadcast
    def receive = {
      case RENDEZVOUS(leader_,rvLoc_)  => {
        if(leader.hashCode <  leader_.hashCode){
          leader = leader_
          rvLoc = rvLoc_
          MOVE_HOST_TO(rvLoc)
          broadcast
        }
      }
      case _ => println("unknown message")
    }
    def broadcast = {
      val bigactors = HOSTED_AT_LINKED_TO_HOST
      bigactors.foreach{b=>
        b ! RENDEZVOUS(leader,rvLoc)
      }
    }
  }
}

case class RENDEZVOUS(leader: ActorRef, location: Place)

