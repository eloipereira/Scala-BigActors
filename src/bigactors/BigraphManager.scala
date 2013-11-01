package bigactors

import edu.berkeley.eloi.bigraph.{Control, BRR, Bigraph, BRS}
import scala.collection.JavaConversions._
import scala.actors.{AbstractActor, Actor}
import edu.berkeley.eloi.bgm2java.Debug
import scala.actors.remote.RemoteActor._
import scala.actors.Actor._
import scala.actors.remote.Node
import java.util.Properties
import java.io.FileInputStream

/**
 * Created with IntelliJ IDEA.
 * User: eloi
 * Date: 6/27/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */

sealed trait BigraphManagerAPI extends Serializable
case class EXECUTE_BRR(brr: BRR) extends BigraphManagerAPI
case object BIGRAPH_REQUEST extends BigraphManagerAPI
case class BIGRAPH_RESPONSE(bigraph: Bigraph) extends BigraphManagerAPI

object BigraphManager extends Actor with App {

  var brs: BRS = new BRS("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true,true)
  val debug = true


  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
  var bigActorSchdl: AbstractActor = BigActorSchdl
  if (remote) bigActorSchdl = select(Node(prop.getProperty("BigActorSchdlIP"),prop.getProperty("BigActorSchdlPort").toInt), 'bigActorSchdl)


  def act() {

    //more configuration
    if (remote){
      val port = prop.getProperty("BigraphManagerPort").toInt
      val ip = prop.getProperty("BigraphManagerIP")
      Debug.println("BigraphManager operating remotely at IP "+ ip + " and port "+ port.toInt,debug)
      //TODO - check if property actually matches with machine's IP
      alive(port)
      register('bigraphManager, self)
    } else {
      Debug.println("BigraphManager operating locally",debug)
    }

    loop{
      react{
        case EXECUTE_BRR(brr) => {
          Debug.println("Old bigraph: " + brs,debug)
          brs.applyRules(List(brr),2)
          Debug.println("New bigraph: " + brs,debug)
        }
        case BIGRAPH_REQUEST => {
          Debug.println("Sending bigraph to scheduler",debug)
          bigActorSchdl ! BIGRAPH_RESPONSE(brs.getBigraph)
        }
      }
    }
  }
  start
}