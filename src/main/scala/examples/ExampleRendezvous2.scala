package bigactors

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.BigActor._

object ExampleRendezvous2 extends App{

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
    val robots = LINKED_TO_HOST
    robots.foreach{r =>
      BigActor hosted_at r with_behavior{
        MOVE_HOST_TO(rvLoc)
      }
    }
  }


}