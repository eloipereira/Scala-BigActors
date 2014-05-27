package bigactors

import bigactors.ExampleRendezvous3.RENDEZVOUS
import edu.berkeley.eloi.bigraph.{Place, BigraphNode, BRR}
import bigactors.BigActor._
import scala.actors.Actor._
import java.util.Properties
import java.io.FileOutputStream

object ExampleRendezvous4 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/robots.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

  //BigActors
  BigActor hosted_at "r0" with_behavior {
    val robots = LINKED_TO_HOST ++ HOST
    robots.foreach{r =>
       BigActor hosted_at r with_behavior rendezvous
    }
  }

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

}