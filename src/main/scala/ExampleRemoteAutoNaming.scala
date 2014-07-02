package bigactors
package remote

import java.nio.file.Paths

import RemoteBigActorImplicits._
import edu.berkeley.eloi.bigraph.{Place, BRR}
import java.util.Properties
import java.io.FileOutputStream

object ExampleRemoteAutoNaming extends App{
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
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

  val uav1 = new RemoteBigActor(Symbol("u1")){
    def behavior() {
      control(new BRR(hostID.name + "_UAV[network].$0 | $1 -> " + hostID.name +"_UAV[network].($0 | " + bigActorID.name + "_BA) | $1"))
      observe(Children(Parent(Host)))
      loop {
        react {
          case obs: Array[Place] => println("New observation for BigActor " + bigActorID.name + ": " + obs)
          case msg: Any => println("New mail for BigActor " + bigActorID.name + ": " + msg)
        }
      }
    }
  }


  val uav0 = new RemoteBigActor(Symbol("u0")){
    def behavior() {
      control(new BRR(hostID.name + "_UAV[network].$0 | $1 -> " + hostID.name + "_UAV[network].($0 | " + bigActorID.name + "_BA) | $1"))
      observe(Children(Parent(Host))) // TODO make it children.linkedTo.host
      Thread.sleep(5000)
      react{
        case obs: Array[Place] => {
          println("New observation for uav0: "+ obs)
          obs.foreach(b =>
            sendMsg("Hello I'm BigActor " + bigActorID.name ,Symbol(b.toString))
          )
          control(new BRR("l0_Location.(u0_UAV[network].$0 | $1) | l1_Location.$2 -> l0_Location.($1) | l1_Location.(u0_UAV[network].$0 | $2)"))
          migrate(Symbol("u1"))
          observe(Host)
          react{
            case obs: Array[Place] => println("New observation for BigActor " + bigActorID.name + ": " + obs)
          }
        }
      }
    }
  }

  uav1.start
  uav0.start
}
