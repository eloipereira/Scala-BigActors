package bigactors
package remote

import java.io.FileInputStream
import java.net._
import java.util.Properties

import edu.berkeley.eloi.bigraph.BRS
import org.apache.commons.logging.{Log, LogFactory}

import scala.actors.Actor
import scala.actors.Actor._
import scala.actors.remote.RemoteActor._
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: eloi
 * Date: 6/27/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */



object RemoteBigraphManager extends Actor with  App {

  // configuration

  private val log: Log = LogFactory.getLog("RemoteBigraphManager")


  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val bgmPath: String = prop.getProperty("bgmPath")

  val visualization = prop.getProperty("visualization").toBoolean
  var brs: BRS = new BRS(bgmPath,visualization)

  val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
  if (!remote){
    log.error("Configuration file not set for remote bigActors")
    System.exit(0)
  }

  def act() {
    val managerID = Symbol(prop.getProperty("BigraphManagerID"))
    val managerPort = prop.getProperty("BigraphManagerPort").toInt
    val localhost = InetAddress.getLocalHost
    val managerIP = localhost.getHostAddress

    alive(managerPort)
    register(managerID, self)
    log.debug("BigraphManager operating remotely at IP "+ managerIP + " and port "+ managerPort)

    loop{
      react{
        case EXECUTE_BRR(brr) => {
          log.debug("Old bigraph: " + brs)
          log.debug("BRR: " + brr)
          brs.applyRules(List(brr))
          log.debug("New bigraph: " + brs)
        }
        case BIGRAPH_REQUEST => {
          log.debug("Sending bigraph to " + sender)
          sender ! BIGRAPH_RESPONSE(brs.getBigraph)
        }
      }
    }
  }
  start
}