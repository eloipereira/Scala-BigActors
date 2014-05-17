import bigactors._

import edu.berkeley.eloi.bigraph.{BigraphNode, BRR}
import bigactors.BigActor._
import scala.actors.Actor._
import java.util.Properties
import java.io.FileOutputStream

object ExampleRendezvous extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/robots.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

  //BigActors
  BigActor hosted_at "r0" with_behavior{
    println(PARENT_HOST)
  }

}
