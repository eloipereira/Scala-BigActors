package scalaBigraph

import scalaBigraph.ScalaBigraphImplicits._
import scalaBigraph.{ScalaBigraphNode, ScalaBigraph}

/**
 * Created by eloi on 7/28/14.
 */
object  ExampleScalaBigraphConstruction extends App{

  case class Vehicle(vehicleName: String, conns: List[String]) extends ScalaBigraphNode(vehicleName, conns)
  case class Location(locationName: String) extends ScalaBigraphNode(locationName,List())


  val loc0 = Location("loc0")
  val loc1 = Location("loc1")
  val loc2 = Location("loc2")

  val robot0 = Vehicle("robot0",List("network"))
  val robot1 = Vehicle("robot1",List("network"))

  val bigraph = loc0 ~ (robot0 | robot1 | $(0) ) || loc1

  println(bigraph)

  loc2 match {
    case loc1 => println(true)
    case _ => println(false)
  }





}
