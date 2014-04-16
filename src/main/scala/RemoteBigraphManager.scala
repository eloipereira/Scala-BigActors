package bigactors
package remote

import edu.berkeley.eloi.bigraph.BRS
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug

import scala.actors._
import scala.actors.Actor._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._

import java.util.Properties
import java.io.FileInputStream
import java.net._

/**
 * Created with IntelliJ IDEA.
 * User: eloi
 * Date: 6/27/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */



object RemoteBigraphManager extends Actor with  App {

  //scala.actors.Debug.level_=(100)

  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val bgmPath: String = prop.getProperty("bgmPath")

  val debug = prop.getProperty("debug").toBoolean
  val visualization = prop.getProperty("visualization").toBoolean
  val log = prop.getProperty("log").toBoolean
  var brs: BRS = new BRS(bgmPath,log,visualization)

  val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
  if (!remote){
    System.err.println("Configuration file not set for remote bigActors",debug)
    System.exit(0)
  }

  def act() {
    val managerID = Symbol(prop.getProperty("BigraphManagerID"))
    val managerPort = prop.getProperty("BigraphManagerPort").toInt
    val localhost = InetAddress.getLocalHost
    val managerIP = localhost.getHostAddress

    alive(managerPort)
    register(managerID, self)
    Debug.println("BigraphManager operating remotely at IP "+ managerIP + " and port "+ managerPort,debug)

    loop{
      react{
        case EXECUTE_BRR(brr) => {
          Debug.println("Old bigraph: " + brs,debug)
          Debug.println("BRR: " + brr,debug)
          brs.applyRules(List(brr),2)
          Debug.println("New bigraph: " + brs,debug)
        }
        case BIGRAPH_REQUEST => {
          Debug.println("Sending bigraph to " + sender,debug)
          sender ! BIGRAPH_RESPONSE(brs.getBigraph)
        }
      }
    }
  }
  start
}