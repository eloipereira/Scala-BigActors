package akkaBigActors.examples

/**
 * Created by eloi on 03-07-2014.
 */

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import akka.util.Timeout
import akkaBigActors.ROSBigraphManager
import bigactors._
import edu.berkeley.eloi.bigraph.BRR

import scala.concurrent.Await
import scala.concurrent.duration._


object ExampleROSBigraphManager extends App{

  implicit val system = ActorSystem("mySystem")

  val bigraphManager = system.actorOf(Props(classOf[ROSBigraphManager], "localhost", 11311))

  implicit val timeout = Timeout(5 seconds)
  val future = bigraphManager ? BIGRAPH_REQUEST
  Await.result(future, timeout.duration) match {
    case BIGRAPH_RESPONSE(bg) => {
      println("[Example]:\t\t\t Hello bigraph, " + bg)
    }
    case _ => println("[Example]:\t\t\t Unknown reply.")
  }

  bigraphManager ! EXECUTE_BRR(new BRR("l0_Location.( $0 | r0_Robot[network])|| l1_Location.($1) -> l0_Location.( $0 ) || l1_Location.($1| r0_Robot[network])"))

  val future1 = bigraphManager ? BIGRAPH_REQUEST
  Await.result(future1, timeout.duration) match {
    case BIGRAPH_RESPONSE(bg) => {
      println("[Example]:\t\t\t New bigraph, " + bg)
    }
    case _ => println("[Example]:\t\t\t Unknown reply.")
  }

}

