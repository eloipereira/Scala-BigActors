package bigactors.examples

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.BigActor
import bigactors.BigActor._
import edu.berkeley.eloi.bigraph.Place

import scala.actors.Actor._

case class RENDEZVOUS_AT_LOCATION(loc: Place)

object ExampleRendezvous0 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.store(new FileOutputStream("config.properties"),null)

  //BigActors
  BigActor hosted_at "r0" with_behavior{
    val rvLoc = PARENT_HOST.head
    r1BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
    r2BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
    r3BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
    r4BA ! RENDEZVOUS_AT_LOCATION(rvLoc)
  }

  val r1BA = BigActor hosted_at "r1" with_behavior receiveLocationAndMove

  val r2BA = BigActor hosted_at "r2" with_behavior receiveLocationAndMove

  val r3BA = BigActor hosted_at "r3" with_behavior receiveLocationAndMove

  val r4BA = BigActor hosted_at "r4" with_behavior receiveLocationAndMove

  def receiveLocationAndMove = {
    react{
      case RENDEZVOUS_AT_LOCATION(loc) => {
        MOVE_HOST_TO(loc)
      }
    }
  }

}