package scalaBigraph

import ScalaBigraphImplicits._

/**
 * Created by eloi on 7/28/14.
 */
object  ExampleScalaBigraphConstruction extends App{

  case class Vehicle(vehicleName: String, conns: List[String]) extends Node(vehicleName,conns)
  case class Location(locationName: String) extends Node(locationName,List())


  val loc0 = Location("loc0")
  val loc1 = Location("loc1")
  val robot0 = Vehicle("robot0",List("network"))
  val robot1 = Vehicle("robot1",List("network"))

  println(loc0 ~> (robot0 | robot1 | $(0) ) || loc1)


  println(
    Location("loc0") ~> (
      Vehicle("robot0",List("network")) |
      Vehicle("robot1",List("network")) |
      $(0))
      ||
    Location("loc1")
  )


}
