package bigactors

import scala.actors.{Actor, OutputChannel}
import edu.berkeley.eloi.bigraph.{BigraphNode, BRR}
import edu.berkeley.eloi.bgm2java.Debug
import java.util.Properties
import java.io.FileInputStream

trait BigActorTrait{
  def observe(query: String) = {
    BigActorSchdl ! OBSERVATION_REQUEST(query)
  }
  def control(brr: BRR){
    BigActorSchdl ! CONTROL_REQUEST(brr)
  }
  def migrate(newHostId: Symbol){
    BigActorSchdl ! MIGRATION_REQUEST(newHostId)
  }
  def sendMsg(msg: Any,rcv: OutputChannel[Any]){
    BigActorSchdl ! SEND_REQUEST(msg,rcv)
  }
}

abstract class BigActor(hostID: Symbol) extends Actor with BigActorTrait with BigActorImplicits {
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val debug = prop.getProperty("debug").toBoolean

  def behavior

  def act{
    BigActorSchdl !  HOSTING_REQUEST(hostID)
    receive{
      case HOSTING_SUCCESSFUL => Debug.println("[BigActor]:\t\t\t BigActor " + this + " successfully hosted at " + hostID,debug)
    }
    behavior
  }
}


object BigActor extends BigActorTrait with BigActorImplicits {
  def bigActor(hostID:Symbol) (body: => Unit): BigActor = new BigActor(hostID) {
    override def behavior: Unit = body
    this.start
  }
}

trait BigActorImplicits{
  implicit def Symbol2BigActorHelper(hostID:Symbol) = new BigActorHelper(hostID)
  def hosted_at (hostName:String):Symbol = Symbol(hostName)
  implicit def StringToBigraphNode (name: String) = new BigraphNode(name)
}

class BigActorHelper(hostID:Symbol){
  def with_behavior(body: => Unit) : BigActor = new BigActor(hostID){
    def behavior = body
    start
  }
}