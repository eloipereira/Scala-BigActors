package bigactors.examples

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.BigActor
import bigactors.BigActor._
import edu.berkeley.eloi.bigraph.Place

import scala.actors.Actor._

object ExampleRendezvous3 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.store(new FileOutputStream("config.properties"),null)

  //BigActors
  BigActor hosted_at "r0" with_behavior rendezvous

  BigActor hosted_at "r1" with_behavior rendezvous

  BigActor hosted_at "r2" with_behavior rendezvous

  BigActor hosted_at "r3" with_behavior rendezvous

  BigActor hosted_at "r4" with_behavior rendezvous

  def rendezvous = {
    var rvLoc = PARENT_HOST.head
    var leader = BigActor.self
    loop{
      Thread.sleep(1000)
      val bigactors = HOSTED_AT_LINKED_TO_HOST
      bigactors.foreach{b=>
        b ! RENDEZVOUS(leader,rvLoc)
      }
      react{
        case RENDEZVOUS(leader_,rvLoc_)  => {
          if(leader.hashCode <  leader_.hashCode){
            leader = leader_
            rvLoc = rvLoc_
            MOVE_HOST_TO(rvLoc)
          }
        }
      }
    }
  }

  case class RENDEZVOUS(leader: BigActor, location: Place)

}