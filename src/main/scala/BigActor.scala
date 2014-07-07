package bigactors

import java.io.FileInputStream
import java.util.Properties

import bigactors.{BigActorSchdl, HOSTING_REQUEST, HOSTING_SUCCESSFUL}
import edu.berkeley.eloi.bgm2java.Debug
import edu.berkeley.eloi.bigraph.{BRR, BigraphNode, Place}

import scala.actors.Actor._
import scala.actors.{Actor, OutputChannel}
import scala.collection.mutable.ArrayBuffer

trait BigActorTrait{
  def observe(query: Query) = {
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

  def observeAndWaitForBigraph(query: Query) = {
    BigActorSchdl ! OBSERVATION_REQUEST(query)
    receive {
      case observation: Array[Place] => observation
    }
  }

  def observeAndWaitForBigActors(query: Query) = {
    BigActorSchdl ! OBSERVATION_REQUEST(query)
    receive {
      case observation: Array[OutputChannel[Any]] => observation
    }
  }

  def HOST = observeAndWaitForBigraph(Host)
  def PARENT_HOST = observeAndWaitForBigraph(Parent(Host))
  def CHILDREN_PARENT_HOST = observeAndWaitForBigraph(Children(Parent(Host)))
  def LINKED_TO_HOST = observeAndWaitForBigraph(Linked_to(Host))
  def HOSTED_AT_LINKED_TO_HOST  = observeAndWaitForBigActors(Hosted_at(Linked_to(Host)))
  def MOVE_HOST_TO(loc: Place) = control(new BRR(PARENT_HOST.head.toBgm + ".( $0 |" + HOST.head.toBgm + ")||" +  loc.toBgm + ".($1) ->" + PARENT_HOST.head.toBgm + ".( $0 ) || " + loc.toBgm +".($1|"+HOST.head.toBgm + ")"))
  // TODO - create CONNECT_HOST_TO(link: String)

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
  def self = Actor.self.asInstanceOf[BigActor]

  def bigActor(hostID:Symbol) (body: => Unit): BigActor = new BigActor(hostID) {
    override def behavior: Unit = body
    this.start
  }
}

trait BigActorImplicits{
  implicit def Place2String(place: Place) = place.getId.asInstanceOf[String]
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