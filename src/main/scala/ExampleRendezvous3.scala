package bigactors

import edu.berkeley.eloi.bigraph.{Place, BigraphNode, BRR}
import bigactors.BigActor._
import scala.actors.Actor._
import java.util.Properties
import java.io.FileOutputStream

object ExampleRendezvous3 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/robots.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

    //BigActors
  BigActor hosted_at "r0" with_behavior rendezvous

  BigActor hosted_at "r1" with_behavior rendezvous

  BigActor hosted_at "r2" with_behavior rendezvous

  BigActor hosted_at "r3" with_behavior rendezvous

  BigActor hosted_at "r4" with_behavior rendezvous


  case class RENDEZVOUS(leader: BigActor, location: Place)

  def rendezvous = {
      var rvLoc = PARENT_HOST.head
      var leader = BigActor.self
      loop{
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