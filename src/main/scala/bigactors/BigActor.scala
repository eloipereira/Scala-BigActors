package bigactors

import java.io.FileInputStream
import java.util.Properties
import edu.berkeley.eloi.bigraph.{Bigraph, BRR, BigraphNode, Place}
import org.apache.commons.logging.{Log, LogFactory}
import scala.actors.Actor._
import scala.actors.{Actor, OutputChannel}
import scala.collection.JavaConversions._


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
      case observation: Bigraph => observation
    }
  }

  def observeAndWaitForBigActors(query: Query) = {
    BigActorSchdl ! OBSERVATION_REQUEST(query)
    receive {
      case observation: Array[OutputChannel[Any]] => observation
    }
  }

  def ALL = observeAndWaitForBigraph(All)
  def HOST = observeAndWaitForBigraph(Host)
  def PARENT_HOST = observeAndWaitForBigraph(Parent(Host))
  def CHILDREN_PARENT_HOST = observeAndWaitForBigraph(Children(Parent(Host)))
  def LINKED_TO_HOST = observeAndWaitForBigraph(Linked_to(Host))
  def HOSTED_AT_LINKED_TO_HOST  = observeAndWaitForBigActors(Hosted_at(Linked_to(Host)))
  def MOVE_HOST_TO(loc: Place) = {
    val parent_host = PARENT_HOST.getNodes.head.toBgm
    val hostStr =  HOST.getNodes.head.toBgm

    val locStr = loc.toBgm
    val brr = new BRR(parent_host + ".( $0 |" + hostStr + ")||" +  locStr + ".($1) ->" + parent_host + ".( $0 ) || " + locStr +".($1|"+ hostStr + ")")
    control(brr)
  }
  def MOVE_HOST_TO2(loc: Place) = control(new BRR( HOST.getNodes.head.toBgm + "|" +  loc.toBgm + ".($1) ->" + loc.toBgm +".($1|"+HOST.getNodes.head.toBgm + ")"))
  def CONNECT_HOST_TO_WLAN(wlan:Place) = {
    val link = wlan.getNames.head
    var node0 = HOST.getNodes.head
    node0.setNames(List(link))
    control(new BRR( node0.toBgm + "|" +  wlan.toBgm + ".($1) ->" + wlan.toBgm +".($1|"+node0.toBgm + ")"))
  }
  def CONNECT_HOST_TO_LINK(link: String) = {
    var node0 = HOST.getNodes.head
    val node0Bgm = node0.toBgm
    var node1 = node0
    node1.setNames(List(link))
    control(new BRR(node0Bgm + "->" + node1.toBgm))
  }

  // TODO - create CONNECT_HOST_TO(link: String)

}

abstract class BigActor(hostID: Symbol) extends Actor with BigActorTrait with BigActorImplicits {
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  private val log: Log = LogFactory.getLog(classOf[BigActor])

  def behavior

  def act{
    BigActorSchdl !  HOSTING_REQUEST(hostID)
    receive{
      case HOSTING_SUCCESSFUL => log.debug("BigActor " + this + " successfully hosted at " + hostID)
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

trait BigActorImplicits extends BigActorTrait{
  implicit def Place2String(place: Place) = place.getId.asInstanceOf[String]
  implicit def Symbol2BigActorHelper(hostID:Symbol) = new BigActorHelper(hostID)
  def hosted_at (hostName:String):Symbol = Symbol(hostName)
  implicit def StringToBigraphNode (name: String) = {
    val obs = ALL
    ALL.asInstanceOf[Bigraph].getNode(name)
  }
}

class BigActorHelper(hostID:Symbol){
  def with_behavior(body: => Unit) : BigActor = new BigActor(hostID){
    def behavior = body
    start
  }
}