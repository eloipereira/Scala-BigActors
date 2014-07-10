package bigactors.akkaBigActors

/**
 * Created by eloi on 03-07-2014.
 */

import java.nio.file.Paths

import akka.actor.ActorDSL._
import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import bigactors.{BIGRAPH_RESPONSE, BIGRAPH_REQUEST, Host, Parent}
import edu.berkeley.eloi.bigraph.{Bigraph, Place}
import org.ros.RosCore

object ExampleROSBigraphManager extends App {
  implicit val system = ActorSystem("mySystem")

  val bigraphManager = system.actorOf(Props(classOf[ROSBigraphManager], "localhost", 11311))



  actor(new Act{
    bigraphManager ! BIGRAPH_REQUEST
    override def receive = {
      case BIGRAPH_RESPONSE(bg) => println(bg)
    }
  }
  )
}

