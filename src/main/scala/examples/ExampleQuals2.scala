package bigactors
package remote

import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.remote.RemoteBigActorImplicits._
import edu.berkeley.eloi.bigraph.Place

object ExampleQuals2 extends App{

  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/qualsEx2.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","true")
  prop.store(new FileOutputStream("config.properties"),null)

  new RemoteBigActor("NomadicObserver","camera0") {
    def behavior() {
      loop {
        observe(Children(Parent(Host)))
        react {
          case obs: Array[Place] => {
            println(obs)
            if (obs.contains("p0") || obs.contains("p1") || obs.contains("p2")){
              println(obs)
            } else{
              observe(Linked_to(Host))
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
