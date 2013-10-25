package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.Predef.String

sealed trait Requests
case class HOSTING(bigActor: BigActor, hostId: Symbol) extends Requests
case class OBSERVE(query: String, bigActorID: Symbol) extends Requests
case class CONTROL(brr: BRR, bigActorID: Symbol) extends Requests
case class SEND(msg: Message, bigActorID: Symbol) extends Requests
case class MIGRATE(newHostId: Symbol, bigActorID: Symbol) extends Requests

abstract class BigActor(val bigActorID: Symbol, val initialHostId: Symbol) extends Actor {

  BigActorSchdl !  HOSTING(this, initialHostId)

  def observe(query: String) = {
    BigActorSchdl ! OBSERVE(query, bigActorID)
  }

  def control(brr: BigraphReactionRule) {
    BigActorSchdl ! CONTROL(brr, bigActorID)
  }

  def migrate(newHostId: Symbol) {
    BigActorSchdl ! MIGRATE(newHostId, bigActorID)
  }

  def send(msg: Message){
    BigActorSchdl ! SEND(msg, bigActorID)
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

  implicit def Name2Symbol(name: Name) = Symbol(name)
  implicit def Name2BigActorIDHelper(bigActorName: Name) = new BigActorIDHelper(bigActorName)
  implicit def BigActorSignature2BigActorHelper(signature: BigActorSignature) = new  BigActorHelper(signature)
  implicit def MessageHeader2MessageHelper(msgHeader: MessageHeader) = new MessageHelper(msgHeader)
  implicit def String2BigraphReactionRule(term: String) = new BigraphReactionRule(term)
  implicit def String2Node(nodeName: String) = new Node(nodeName)

  class BigActorIDHelper(bigActorName: Name){
    def hosted_at(hostName:Name): BigActorSignature = (Symbol(bigActorName),Symbol(hostName))
    def send_message(msg: Any): MessageHeader = (Symbol(bigActorName),msg)

    def observe(query: String) = {
      BigActorSchdl ! OBSERVE(query, Symbol(bigActorName))
    }

    def control(brr: BigraphReactionRule) {
      BigActorSchdl ! CONTROL(brr, Symbol(bigActorName))
    }

    def migrate(newHostId: Symbol) {
      BigActorSchdl ! MIGRATE(newHostId, Symbol(bigActorName))
    }

    def send(msg: Message){
      BigActorSchdl ! SEND(msg, Symbol(bigActorName))
    }
  }

  class BigActorHelper(signature: BigActorSignature){
    def with_behavior (body : => Unit): BigActor = new BigActor(signature._1,signature._2) {
      def act() = body
    }
  }

  class MessageHelper(msgHeader: MessageHeader) {
    def to(rcv: Name) = msgHeader._1.name send(new Message(rcv,msgHeader._2))
  }

}
