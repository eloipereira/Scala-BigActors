package bigactors
package remote

import bigactors.Observation
import edu.berkeley.eloi.bigraph.BRR
import java.util.Properties
import java.io.FileOutputStream


object ExampleRemote extends App{
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
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/simple.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)


  val uav1 = new RemoteBigActor( Symbol("uav1"), Symbol("u1")){
    def behavior() {
      control(new BRR("u1_UAV[network].$0 | $1 -> u1_UAV[network].($0 | uav1_BA) | $1"))
      observe("children.parent.host")
      loop {
        react {
          case obs: Observation => println("New observation for uav1: " + obs)
          case msg: Any => println("New mail for uav1: " + msg)
        }
      }
    }
  }


  val uav0 = new RemoteBigActor( Symbol("uav0"), Symbol("u0")){
    def behavior() {
      control(new BRR("u0_UAV[network].$0 | $1 -> u0_UAV[network].($0 | uav0_BA) | $1"))
      observe("children.linkedTo.host")
      Thread.sleep(5000)
      react{
        case obs: Observation => {
          println("New observation for uav0: "+ obs)
          obs.bigraph.foreach(b =>
            sendMsg("Hello I'm BigActor " + bigActorID,Symbol(b.toString))
          )
          control(new BRR("l0_Location.(u0_UAV[network].$0 | $1) | l1_Location.$2 -> l0_Location.($1) | l1_Location.(u0_UAV[network].$0 | $2)"))
          migrate(Symbol("u1"))
          observe("host")
          react{
            case obs: Observation => println("New observation for uav0: "+ obs)
          }
        }
      }
    }
  }

  uav1.start
  uav0.start

}
