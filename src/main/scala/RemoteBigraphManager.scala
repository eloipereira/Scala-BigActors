package bigactors

import edu.berkeley.eloi.bigraph.{Control, BRR, Bigraph, BRS}
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


  val port = prop.getProperty("BigraphManagerPort").toInt

  val localhost = InetAddress.getLocalHost
  val ip = localhost.getHostAddress // prop.getProperty("BigraphManagerIP")

  def act() {
    Debug.println("BigraphManager operating remotely at IP "+ ip + " and port "+ port.toInt,debug)
    alive(port)
    register('bigraphManager, self)
    loop{
      react{
        case EXECUTE_BRR(brr) => {
          Debug.println("Old bigraph: " + brs,debug)
          Thread.sleep(2000)
          brs.applyRules(List(brr),2)
          Debug.println("New bigraph: " + brs,debug)
        }
        case REMOTE_BIGRAPH_REQUEST(name,ip,port) => {
          val requester = select(Node(ip,port),name)
          Debug.println("Sending bigraph to " + requester,debug)
          requester ! BIGRAPH_RESPONSE(brs.getBigraph)
        }
      }
    }
  }
  start
}