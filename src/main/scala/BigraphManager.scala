package bigactors

import java.io.FileInputStream
import java.util.Properties

import edu.berkeley.eloi.bigraph.BRS
import edu.berkeley.eloi.concreteBgm2Java.Debug
import scala.collection.JavaConversions._

import scala.actors.Actor

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
  val debug: Boolean = prop.getProperty("debug").toBoolean
  val visualization: Boolean = prop.getProperty("visualization").toBoolean
  val log: Boolean = prop.getProperty("log").toBoolean

  var brs: BRS = new BRS(bgmPath,log,visualization)


  def act() {

    Debug.println("[BigraphManager]:\t Interfacing with bigraph at " + bgmPath,debug)

    loop{
      react{
        case EXECUTE_BRR(brr) => {
          Debug.println("[BigraphManager]:\t Old bigraph: " + brs,debug)
          Thread.sleep(2000)
          brs.applyRules(List(brr))
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