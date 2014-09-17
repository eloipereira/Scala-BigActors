package akkaBigActors.examples

/**
 * Created by eloi on 03-07-2014.
 */

import java.nio.file.Paths

import akka.actor.ActorDSL._
import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akkaBigActors.{BigActor, BigActorSchdl, BigMCBigraphManager}
import bigactors.{BIGRAPH_REQUEST, Host, Parent}
import edu.berkeley.eloi.bigraph.Place

object ExampleAkka extends App {
  implicit val system = ActorSystem("mySystem")

  val myActor = system.actorOf(Props[MyActor])


  myActor ! "test"
  myActor ! "test"
  myActor ! "something else"


  val bigraphManager = system.actorOf(Props(classOf[BigMCBigraphManager], Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm").toString, false))

  actor(new Act{
    bigraphManager ! BIGRAPH_REQUEST
    override def receive = {
      case msg => println(msg)
    }
  }
  )

  val bigraphScheduler = system.actorOf(Props(classOf[BigActorSchdl], bigraphManager))

  class MyBigActor(host: Symbol) extends BigActor(host,bigraphScheduler){

    val log = Logging(context.system, this)
    observe(Parent(Host))
    self ! "hello"
    def receive = {
      case obs: Array[Place] => log.info("received bigraph: " + obs.head)
      case msg: Any => log.info("received unknown message: " + msg)
    }
  }

  val a0 = system.actorOf(Props(classOf[MyBigActor], 'r0))

}




class MyActor extends Actor{
  val log = Logging(context.system, this)
  def receive = {
    case "test" => log.info("received test")
    case _ => log.info("received unknown message")
  }
}