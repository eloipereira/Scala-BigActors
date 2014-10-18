package bigactors.examples

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties
import scala.collection.JavaConversions._


import bigactors.{Host, Parent, Children, BigActor}
import bigactors.BigActor._
import edu.berkeley.eloi.bigraph.{BigraphNode, Bigraph, BRR, Place}
import org.apache.commons.logging.{LogFactory, Log}

import scala.actors.Actor._

object ExampleSmartphone extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/smartphone.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.store(new FileOutputStream("config.properties"),null)

  private val log: Log = LogFactory.getLog("ExampleSmartphone")
  //BigActors

  val server = BigActor hosted_at "srv" with_behavior{
    loop{
      react{
        case msg: Bigraph => log.info("New msg: " + msg)
      }
    }
  }

  val app = BigActor hosted_at "sp" with_behavior{

    MOVE_HOST_TO("street1")
    observe(Children(Parent(Host)))
    react {
      case obs: Bigraph => {
        log.info("New obs: " + obs)
        MOVE_HOST_TO("street3")
        CONNECT_HOST_TO_WLAN("wlan0")
        sendMsg(obs,server)
      }
    }
  }
}
