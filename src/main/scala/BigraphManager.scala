package bigactors

import edu.berkeley.eloi.bigraph.{Control, BRR, Bigraph, BRS}
import scala.collection.JavaConversions._
import scala.actors.{AbstractActor, Actor}
import edu.berkeley.eloi.bgm2java.Debug
import scala.actors.remote.RemoteActor._
import scala.actors.Actor._
import java.util.Properties
import java.io.FileInputStream

/**
 * Created with IntelliJ IDEA.
 * User: eloi
 * Date: 6/27/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */

object BigraphManager extends Actor {



  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val bgmPath: String = prop.getProperty("bgmPath")
  val debug = prop.getProperty("debug").toBoolean
  val visualization = prop.getProperty("visualization").toBoolean
  val log = prop.getProperty("log").toBoolean

  var brs: BRS = new BRS(bgmPath,log,visualization)


  def act() {

    Debug.println("[BigraphManager]:\t Interfacing with bigraph at " + bgmPath,debug)

    loop{
      react{
        case EXECUTE_BRR(brr) => {
          Debug.println("[BigraphManager]:\t Old bigraph: " + brs,debug)
          Thread.sleep(2000)
          brs.applyRules(List(brr),2)
          Debug.println("[BigraphManager]:\t New bigraph: " + brs,debug)
        }
        case BIGRAPH_REQUEST => {
          Debug.println("[BigraphManager]:\t Sending bigraph: " + brs,debug)
          sender ! BIGRAPH_RESPONSE(brs.getBigraph)
        }
      }
    }
  }
  start
}