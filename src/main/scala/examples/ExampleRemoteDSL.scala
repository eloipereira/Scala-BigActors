package bigactors
package remote

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.remote.RemoteBigActorImplicits._
import edu.berkeley.eloi.bigraph.Place

import scala.actors.Actor._

object ExampleRemoteDSL extends App{
  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","true")
  prop.setProperty("BigActorSchdlIP","172.21.5.61")
  prop.setProperty("BigActorSchdlPort","3000")
  prop.setProperty("BigActorSchdlID","bigActorSchdl")
  prop.setProperty("BigraphManagerIP","172.21.5.61")
  prop.setProperty("BigraphManagerID","bigraphManager")
  prop.setProperty("BigraphManagerPort","3001")
  prop.setProperty("BigActorsPort","3000")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/simple.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.store(new FileOutputStream("config.properties"),null)


  "uav1" hosted_at "u1" with_behavior {
    "uav1" observe Children(Parent(Host))
    loop {
      react {
        case obs: Array[Place] => println("New observation for uav1: " + obs)
        case msg: Any => println("New message for uav1: " + msg)
      }
    }
  }

  "uav0" hosted_at "u0" with_behavior
    {
      "uav0" observe Children(Parent(Host))
      react{
        case obs: Array[Place] => {
          println("New observation for uav0: "+ obs)
          "uav0" send_message "Hello I'm a BigActor!" to "uav1"
          "uav0" control "l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"
          "uav0" migrate "u1"
          "uav0" observe Host
          receive {
            case obs: Array[Place] => println("New observation for uav0: "+ obs)
          }
          "uav0" control "l0_Location[x].(u1_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u1_UAV[z] | $1)"
          "uav0" observe Children(Parent(Host))
          receive {
            case obs: Array[Place] => println("New observation for uav0: "+ obs)
          }
        }
      }
    }
}
