package bigactors.akka

/**
 * Created by eloi on 03-07-2014.
 */

import java.nio.file.Paths

import akka.actor.ActorDSL._
import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import bigactors.BIGRAPH_REQUEST

object ExampleAkka extends App {
  implicit val system = ActorSystem("mySystem")

  val myActor = system.actorOf(Props[MyActor])


  myActor ! "test"
  myActor ! "test"
  myActor ! "something else"


  val bigraphManager = system.actorOf(Props(classOf[AkkaBigraphManager], Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm").toString, false, false))

  actor(new Act{
    bigraphManager ! BIGRAPH_REQUEST
    override def receive = {
      case msg => println(msg)
    }
  }

  )
}


class MyActor extends Actor{
  val log = Logging(context.system, this)
  def receive = {
    case "test" => log.info("received test")
    case _ => log.info("received unknown message")
  }
}