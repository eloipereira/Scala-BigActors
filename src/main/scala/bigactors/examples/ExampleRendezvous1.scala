package bigactors.examples

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties
import bigactors.BigActor
import bigactors.BigActor._
import scala.actors.Actor._
import scala.collection.JavaConversions._


object ExampleRendezvous1 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.store(new FileOutputStream("config.properties"),null)

  //BigActors

  BigActor hosted_at "r0" with_behavior observeAndRequestRV

  BigActor hosted_at "r1" with_behavior receiveLocationAndMove

  BigActor hosted_at "r2" with_behavior receiveLocationAndMove

  BigActor hosted_at "r3" with_behavior receiveLocationAndMove

  BigActor hosted_at "r4" with_behavior receiveLocationAndMove

  def observeAndRequestRV = {
      val rvLoc = PARENT_HOST.getNodes.head
      val bigactors = HOSTED_AT_LINKED_TO_HOST
      bigactors.foreach{b=>
        b ! RENDEZVOUS_AT_LOCATION(rvLoc)
      }
    }

  def receiveLocationAndMove = {
    react{
      case RENDEZVOUS_AT_LOCATION(loc) => {
        MOVE_HOST_TO(loc)
      }
    }
  }



}