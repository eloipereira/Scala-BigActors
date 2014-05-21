package bigactors

import bigactors._
import edu.berkeley.eloi.bigraph.{Place, BigraphNode, BRR}
import bigactors.BigActor._
import scala.actors.Actor._
import java.util.Properties
import java.io.FileOutputStream

object ExampleSimple extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/simple.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

 //BigActors
  val uav1 = BigActor hosted_at "u1" with_behavior{
    observe("children.parent.host")
    loop {
      react {
        case observation: Array[Place] => {
          if (observation contains "u0") println("I observed u0")

        }
        case msg: Any => println("New mail for uav1: " + msg)
      }
    }
  }

  val uav0 = bigActor(Symbol("u0")){
    observe("children.parent.host")
    react{
      case obs: Array[Place] => {
        println("New observation for uav0: "+ obs)
        sendMsg("Hello I'm a BigActor!",uav1)
        Thread.sleep(1000)
        control(new BRR("l0_Location.(u0_UAV[network] | $0) | l1_Location.$1 -> l0_Location.$0 | l1_Location.(u0_UAV[network] | $1)"))
        migrate(Symbol("u1"))
        observe("host")
        react{
          case obs: Array[Place] => println("New observation for uav0: "+ obs)
        }
      }
    }
  }

}
