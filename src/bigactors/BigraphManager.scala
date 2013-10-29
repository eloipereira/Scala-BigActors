package bigactors

import edu.berkeley.eloi.bigraph.{Control, BRR, Bigraph, BRS}
import scala.collection.JavaConversions._
import scala.actors.Actor
import edu.berkeley.eloi.bgm2java.Debug
import scala.actors.remote.RemoteActor._
import scala.actors.Actor._
import scala.actors.remote.Node

/**
 * Created with IntelliJ IDEA.
 * User: eloi
 * Date: 6/27/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */

sealed trait BigraphManagerAPI extends Serializable
case class EXECUTE_BRR(brr: BRR) extends BigraphManagerAPI
case object GET_BIGRAPH extends BigraphManagerAPI
case class BIGRAPH_RESPONSE(bigraph: Bigraph) extends BigraphManagerAPI

object BigraphManager extends Actor {

  var brs: BRS = new BRS("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true,true)
  val debug = true
  def act{

    alive(9010)
    register('bigraphManager, self)

    val bigActorSchdl = select(Node("localhost",9010), 'bigActorSchdl)

    loop{
      react{
        case EXECUTE_BRR(brr) => {
          Debug.println("Old bigraph: " + brs,debug)
          brs.applyRules(List(brr),2)
          Debug.println("New bigraph: " + brs,debug)
        }
        case GET_BIGRAPH => {
          Debug.println("Sending bigraph to scheduler",debug)
          bigActorSchdl ! BIGRAPH_RESPONSE(brs.getBigraph)
        }
      }
    }
  }

  start
}