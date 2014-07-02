package bigactors
package remote

import java.nio.file.Paths

import RemoteBigActorImplicits._
import java.util.Properties
import java.io.FileOutputStream

object ExampleQuals1 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/qualsEx1.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

  new RemoteBigActor("observer","camera0") {
    def behavior() {
      loop {
        observe(Children(Parent(Host)))
        react {
          case obs => println(obs)
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
