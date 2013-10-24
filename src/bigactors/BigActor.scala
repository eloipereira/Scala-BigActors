package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.Predef.String


trait BareBigActor{
  val bigActorID: Symbol
  val initialHostId: Symbol
  def observe(query: String)
  def control(u: BigraphReactionRule)
  def migrate(newHostId: Symbol)
  def send(msg: Message)
}

abstract class BigActor(val bigActorID: Symbol, val initialHostId: Symbol) extends Actor with BareBigActor{

  BigActorSchdl ! ("HOSTING", initialHostId,bigActorID, this)

  override def observe(query: String) = {
    BigActorSchdl ! ("OBSERVE",query, bigActorID)
  }

  override def control(u: BigraphReactionRule) {
    BigActorSchdl ! ("CONTROL",u, bigActorID)
  }

  override def migrate(newHostId: Symbol) {
    BigActorSchdl ! ("MIGRATE", bigActorID,newHostId)
  }

  override def send(msg: Message){
    BigActorSchdl ! ("SEND",msg)
  }

  override def !(msg:Any){
    msg match {
      case x@(m: Message) => BigActorSchdl ! ("SEND", m)
      case x@("SEND_SUCCESSFUL",m:Message) => super.!(m)
      case x@("OBSERVATION_SUCCESSFUL",o:Observation) => super.!(o)
      case _ =>
    }
  }

  override
  def toString: String =  bigActorID.toString
}

case class HOSTING(hostId: Symbol, bigActorId: Symbol)


object BigActor{
  def apply(bigActorID: Symbol,  initialHostId: Symbol, body: => Unit) = {
    val b = new BigActor( bigActorID: Symbol,  initialHostId: Symbol) {
      override def act() = body
    }
    b
  }

  def bigActor(id: Symbol)(hostId: Symbol)(body: => Unit): BigActor = {
    val b = new BigActor(id,hostId){
      override def act() = body
    }
    b
  }
}


object BigActorImplicits {
  type BigActorSignature = (Symbol, Symbol)
  type Name = String
  type MessageHeader = (Symbol,Any)
  //type NameNodeParentTuple = (Name, Node, Node)

  implicit def Name2Symbol(name: Name) = Symbol(name)
  implicit def Name2BigActorIDHelper(bigActorName: Name) = new BigActorIDHelper(bigActorName)
  implicit def BigActorSignature2BigActorHelper(signature: BigActorSignature) = new  BigActorHelper(signature)
  implicit def MessageHeader2MessageHelper(msgHeader: MessageHeader) = new MessageHelper(msgHeader)
  implicit def String2BigraphReactionRule(term: String) = new BigraphReactionRule(term)
  implicit def String2Node(nodeName: String) = new Node(nodeName)

  class BigActorIDHelper(bigActorName: Name){
    def hosted_at(hostName:Name): BigActorSignature = (Symbol(bigActorName),Symbol(hostName))
    def send_message(msg: Any): MessageHeader = (Symbol(bigActorName),msg)

    def observe(query: String) {
      BigActorSchdl ! ("OBSERVE",query, Symbol(bigActorName))
    }

    def control(u: BigraphReactionRule) {
      BigActorSchdl ! ("CONTROL",u, Symbol(bigActorName))
    }

    def migrate(newHostId: Symbol) {
      BigActorSchdl ! ("MIGRATE", Symbol(bigActorName),newHostId)
    }

    def send(msg: Message){
      BigActorSchdl ! ("SEND",msg)
    }
  }

  class BigActorHelper(signature: BigActorSignature){
    def with_behavior (body : => Unit): BigActor = new BigActor(signature._1,signature._2) {
      def act() = body
    }
  }

  class MessageHelper(msgHeader: MessageHeader) {
    def to(rcv: Name) = msgHeader._1.name send(new Message(msgHeader._1,rcv,msgHeader._2))
  }

}
