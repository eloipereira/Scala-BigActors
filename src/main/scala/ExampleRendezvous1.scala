package bigactors

import edu.berkeley.eloi.bigraph.{Place, BigraphNode, BRR}
import bigactors.BigActor._
import scala.actors.Actor._
import java.util.Properties
import java.io.FileOutputStream

object ExampleRendezvous1 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/robots.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

  //BigActors

  val r0BA = BigActor hosted_at "r0" with_behavior{
      val rvLoc = PARENT_HOST
      val bigactors = HOSTED_AT_LINKED_TO_HOST
      bigactors.foreach{b=>
        println(b)
        b ! rvLoc.head
      }
    }

  val r1BA = BigActor hosted_at "r1" with_behavior{
    react{
      case loc: Place => {
        MOVE_HOST_TO(loc)
      }
    }
  }

  val r2BA = BigActor hosted_at "r2" with_behavior{
    react{
      case loc: Place => {
        MOVE_HOST_TO(loc)
      }
    }
  }

  val r3BA = BigActor hosted_at "r3" with_behavior{
    react{
      case loc: Place => {
        MOVE_HOST_TO(loc)
      }
    }
  }

  val r4BA = BigActor hosted_at "r4" with_behavior{
    react{
      case loc: Place => {
        MOVE_HOST_TO(loc)
      }
    }
  }




}