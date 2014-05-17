package bigactors
package remote

import RemoteBigActorImplicits._
import java.util.Properties
import java.io.FileOutputStream

object ExampleQuals1 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/qualsEx1.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

  new RemoteBigActor("observer","camera0") {
    def behavior() {
      loop {
        observe("children.parent.host")
        react {
          case obs: Observation => println(obs)
        }
      }
    }
  }

  new RemoteBigActor("env","room0"){
    def behavior(){
      control("room0_Room.$0 -> room0_Room.(p0_Person|$0)")
      control("room0_Room.$0 -> room0_Room.(p1_Person|$0)")
      control("room0_Room.$0 -> room0_Room.(p2_Person|$0)")
    }
  }
}
