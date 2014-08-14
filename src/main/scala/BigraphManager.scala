package bigactors

import java.io.FileInputStream
import java.util.Properties

import edu.berkeley.eloi.bigraph.BRS
import org.apache.commons.logging.{Log, LogFactory}

import scala.actors.Actor
import scala.collection.JavaConversions._

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
  val visualization: Boolean = prop.getProperty("visualization").toBoolean

  private val log: Log = LogFactory.getLog("BigraphManager")


  var brs: BRS = new BRS(bgmPath,visualization)


  def act() {

    log.debug("Interfacing with bigraph at " + bgmPath)

    loop{
      react{
        case EXECUTE_BRR(brr) => {
          log.debug("Old bigraph: " + brs)
          Thread.sleep(2000)
          brs.applyRules(List(brr))
          log.debug("New bigraph: " + brs)
        }
        case BIGRAPH_REQUEST => {
          log.debug("Bigraph requested. Sending bigraph: " + brs)
          sender ! BIGRAPH_RESPONSE(brs.getBigraph)
        }
      }
    }
  }
  start
}