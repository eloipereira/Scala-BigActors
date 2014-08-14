package bigactors

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.BigActor._
import edu.berkeley.eloi.bigraph.{BRR, Place}

import scala.actors.Actor._

object ExampleSimple extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/simple.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.store(new FileOutputStream("config.properties"),null)

 //BigActors
  val uav1 = BigActor hosted_at "u1" with_behavior{
    observe(Children(Parent(Host)))
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
    observe(Children(Parent(Host)))
    react{
      case obs: Array[Place] => {
        println("New observation for uav0: "+ obs)
        sendMsg("Hello I'm a BigActor!",uav1)
        Thread.sleep(1000)
        control(new BRR("l0_Location.(u0_UAV[network] | $0) | l1_Location.$1 -> l0_Location.$0 | l1_Location.(u0_UAV[network] | $1)"))
        migrate(Symbol("u1"))
        observe(Host)
        react{
          case obs: Array[Place] => println("New observation for uav0: "+ obs)
        }
      }
    }
  }

}
