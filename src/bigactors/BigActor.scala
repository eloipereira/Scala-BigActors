package bigactors

import scala.actors.{AbstractActor, Actor}
import edu.berkeley.eloi.bigraph._
import scala.actors.Actor._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._
import edu.berkeley.eloi.bgm2java.Debug
import java.util.Properties
import java.io.FileInputStream

trait BigActorTrait{
  val bigActorID: Symbol
  var bigActorSchdl: AbstractActor

  def observe(query: String) = {
    bigActorSchdl ! OBSERVATION_REQUEST(query, bigActorID)
  }

  def control(brr: BRR){
    bigActorSchdl ! CONTROL_REQUEST(brr, bigActorID)
  }

  def migrate(newHostId: Symbol){
    bigActorSchdl ! MIGRATION_REQUEST(newHostId, bigActorID)
  }

  def send(msg: Message){
    bigActorSchdl ! SEND_REQUEST(msg, bigActorID)
  }

}

abstract class BigActor(val bigActorID: Symbol, val initialHostId: Symbol) extends Actor with BigActorTrait {


  val debug: Boolean = true

  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
  var bigActorSchdl: AbstractActor = BigActorSchdl
  if (remote) bigActorSchdl = select(Node(prop.getProperty("BigActorSchdlIP"),prop.getProperty("BigActorSchdlPort").toInt), 'bigActorSchdl)



  //  override def !(msg:Any){
  //    msg match {
  //      case m: Message => bigActorSchdl ! SEND_REQUEST(m,bigActorID)
  //      case _ => super.!(msg)
  //    }
  //  }

  def behavior()

  def act() = {
    //more configuration
    if (remote){
      val port = prop.getProperty(bigActorID.name + "Port").toInt
      val ip = prop.getProperty(bigActorID.name + "IP")
      Debug.println("BigActor " +bigActorID.name+ " operating remotely at IP "+ ip + " and port "+ port.toInt,debug)
      //TODO - check if property actually matches with machine's IP
      alive(port)
      register(bigActorID, self)

      bigActorSchdl ! HOSTING_REQUEST_(initialHostId, bigActorID)
      receive{
        case HOSTING_SUCCESSFUL => Debug.println("BigActor successfully hosted",debug)
      }
    } else {
      Debug.println("BigActor " +bigActorID.name+ " operating locally",debug)

      bigActorSchdl !  HOSTING_REQUEST(initialHostId, bigActorID, this)
      receive{
        case HOSTING_SUCCESSFUL => Debug.println("BigActor successfully hosted",debug)
      }
    }



    behavior
  }

  override
  def toString: String =  bigActorID.name
}


object BigActor{
  def apply(bigActorID: Symbol,  initialHostId: Symbol, body: => Unit) = {
    val b = new BigActor( bigActorID: Symbol,  initialHostId: Symbol) {
      override def behavior() = body
    }
    b
  }

  def bigActor(id: Symbol)(hostId: Symbol)(body: => Unit): BigActor = {
    val b = new BigActor(id,hostId){
      override def behavior() = body
    }
    b
  }
}


object BigActorImplicits {
  type BigActorSignature = (Symbol, Symbol)
  type Name = String
  type MessageHeader = (Symbol,Any)

  implicit def Name2Symbol(name: Name) = Symbol(name)
  implicit def Name2BigActorIDHelper(bigActorName: Name) = new BigActorIDHelper(bigActorName)
  implicit def BigActorSignature2BigActorHelper(signature: BigActorSignature) = new  BigActorHelper(signature)
  implicit def MessageHeader2MessageHelper(msgHeader: MessageHeader) = new MessageHelper(msgHeader)
  implicit def String2BigraphReactionRule(term: String) = new BRR(term)
  implicit def String2Node(nodeName: String) = new BigraphNode(nodeName)

  class BigActorIDHelper(bigActorName: Name) extends BigActorTrait{

    val bigActorID = Symbol(bigActorName)

    // configuration
    val prop = new Properties
    prop.load(new FileInputStream("config.properties"))
    val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
    var bigActorSchdl: AbstractActor = BigActorSchdl
    if (remote) bigActorSchdl = select(Node(prop.getProperty("BigActorSchdlIP"),prop.getProperty("BigActorSchdlPort").toInt), 'bigActorSchdl)

    def hosted_at(hostName:Name): BigActorSignature = (bigActorID,Symbol(hostName))
    def send_message(msg: Any): MessageHeader = (bigActorID,msg)

  }

  class BigActorHelper(signature: BigActorSignature){
    def with_behavior (body : => Unit): BigActor = new BigActor(signature._1,signature._2) {
      def behavior() = body

      start
    }
  }

  class MessageHelper(msgHeader: MessageHeader) {
    def to(rcv: Name) = msgHeader._1.name send(new Message(rcv,msgHeader._2))
  }

}
