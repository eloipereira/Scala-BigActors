package bigactors
package remote

import RemoteBigActorImplicits._
import java.util.Properties
import java.io.FileOutputStream
import edu.berkeley.eloi.bigraph.Place

object ExampleQuals2 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/qualsEx2.bgm")
  prop.setProperty("visualization","true")
  prop.setProperty("debug","true")
  prop.setProperty("log","false")
  prop.store(new FileOutputStream("config.properties"),null)

  new RemoteBigActor("NomadicObserver","camera0") {
    def behavior() {
      loop {
        observe("children.parent.host")
        react {
          case obs: Array[Place] => {
            println(obs)
            if (obs.contains("p0") || obs.contains("p1") || obs.contains("p2")){
              println(obs)
            } else{
              observe("linkedTo.host")
              react {
                case obs1: Array[Place] => migrate(obs1.last.getId.asInstanceOf[Symbol])
              }
            }
          }
        }
      }
    }
  }

}
