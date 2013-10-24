package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.Predef.String


trait BareBigActor{
  val bigActorID: BigActorID
  val initialHostId: HostID
  def observe(query: String)
  def control(u: BigraphReactionRule)
  def migrate(newHostId: HostID)
  def send(msg: Message)
}

abstract class BigActor(val bigActorID: BigActorID, val initialHostId: HostID) extends Actor with BareBigActor{

  Initializer.scheduler ! ("HOSTING", initialHostId,bigActorID, this)

  override def observe(query: String) = {
    Initializer.scheduler ! ("OBSERVE",query, bigActorID)
  }

  override def control(u: BigraphReactionRule) {
    Initializer.scheduler ! ("CONTROL",u, bigActorID)
  }

  override def migrate(newHostId: HostID) {
    Initializer.scheduler ! ("MIGRATE", bigActorID,newHostId)
  }

  override def send(msg: Message){
    Initializer.scheduler ! ("SEND",msg)
  }

  override def !(msg:Any){
    msg match {
      case x@(m: Message) => Initializer.scheduler ! ("SEND", m)
      case x@("SEND_SUCCESSFUL",m:Message) => super.!(m)
      case x@("OBSERVATION_SUCCESSFUL",o:Observation) => super.!(o)
      case _ =>
    }
  }

  override
  def toString: String =  bigActorID.toString
}

case class HOSTING(hostId: HostID, bigActorId: BigActorID)


object BigActor{
  def apply(bigActorID: BigActorID,  initialHostId: HostID, body: => Unit) = {
    val b = new BigActor( bigActorID: BigActorID,  initialHostId: HostID) {
      override def act() = body
    }
    b
  }

  def bigActor(id: BigActorID)(hostId: HostID)(body: => Unit): BigActor = {
    val b = new BigActor(id,hostId){
      override def act() = body
    }
    b
  }
}


object BigActorImplicits {
  type BigActorSignature = (BigActorID, HostID)
  type Name = String
  type MessageHeader = (BigActorID,Any)
  //type NameNodeParentTuple = (Name, Node, Node)

  implicit def Name2HostID(name: Name) = HostID(name)
  implicit def Name2BigActorID(name: Name) = BigActorID(name)
  implicit def Name2BigActorIDHelper(bigActorName: Name) = new BigActorIDHelper(bigActorName)
  implicit def BigActorSignature2BigActorHelper(signature: BigActorSignature) = new  BigActorHelper(signature)
  implicit def MessageHeader2MessageHelper(msgHeader: MessageHeader) = new MessageHelper(msgHeader)
  implicit def String2BigraphReactionRule(term: String) = new BigraphReactionRule(term)
  implicit def String2Node(nodeName: String) = new Node(nodeName)

  class BigActorIDHelper(bigActorName: Name){
    def hosted_at(hostName:Name): BigActorSignature = (BigActorID(bigActorName),HostID(hostName))
    def send_message(msg: Any): MessageHeader = (BigActorID(bigActorName),msg)

    def observe(query: String) {
      Initializer.scheduler ! ("OBSERVE",query, BigActorID(bigActorName))
    }

    def control(u: BigraphReactionRule) {
      Initializer.scheduler ! ("CONTROL",u, BigActorID(bigActorName))
    }

    def migrate(newHostId: HostID) {
      Initializer.scheduler ! ("MIGRATE", BigActorID(bigActorName),newHostId)
    }

    def send(msg: Message){
      Initializer.scheduler ! ("SEND",msg)
    }
  }

  class BigActorHelper(signature: BigActorSignature){
    def with_behavior (body : => Unit): BigActor = new BigActor(signature._1,signature._2) {
      def act() = body
    }
  }

  class MessageHelper(msgHeader: MessageHeader) {
    def to(rcv: Name) = msgHeader._1.getName send(new Message(msgHeader._1,rcv,msgHeader._2))
  }

}
