package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.Predef.String
import scala.actors.Actor._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._

abstract class BigActor(val bigActorID: Symbol, val initialHostId: Symbol) extends Actor {

  BigActorSchdl ! HOSTING_REQUEST(this, initialHostId)

  def observe(query: String) = {
    BigActorSchdl ! OBSERVATION_REQUEST(query, bigActorID)
  }

  def control(brr: BRR) {
    BigActorSchdl ! CONTROL_REQUEST(brr, bigActorID)
  }

  def migrate(newHostId: Symbol) {
    BigActorSchdl ! MIGRATION_REQUEST(newHostId, bigActorID)
  }

  def send(msg: Message){
    BigActorSchdl ! SEND_REQUEST(msg, bigActorID)
  }

  override def !(msg:Any){
    msg match {
      case m: Message => BigActorSchdl ! SEND_REQUEST(m,bigActorID)
      case _ => super.!(msg)
    }
  }

//  TODO - this is an idea for specifying the behavior of remote bigActors
//  def behavior()
//  override
//  def act() = {
//    behavior
//    alive(9010)
//      register(bigActorID, self)
//  }
//
  override
  def toString: String =  bigActorID.name
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
  implicit def String2BigraphReactionRule(term: String) = new BRR(term)
  implicit def String2Node(nodeName: String) = new BigraphNode(nodeName)

  class BigActorIDHelper(bigActorName: Name){

    val bigActorSchdl = select(Node("localhost",9010), 'bigActorSchdl)

    def hosted_at(hostName:Name): BigActorSignature = (Symbol(bigActorName),Symbol(hostName))
    def send_message(msg: Any): MessageHeader = (Symbol(bigActorName),msg)

    def observe(query: String) = {
      bigActorSchdl ! OBSERVATION_REQUEST(query, Symbol(bigActorName))
    }

    def control(brr: BRR) {
      bigActorSchdl ! CONTROL_REQUEST(brr, Symbol(bigActorName))
    }

    def migrate(newHostId: Symbol) {
      bigActorSchdl ! MIGRATION_REQUEST(newHostId, Symbol(bigActorName))
    }

    def send(msg: Message){
      bigActorSchdl ! SEND_REQUEST(msg, Symbol(bigActorName))
    }
  }

  class BigActorHelper(signature: BigActorSignature){
    def with_behavior (body : => Unit): BigActor = new BigActor(signature._1,signature._2) {
      def act() = {
        alive(9010)
        register(bigActorID, self)

        body

      }
    }
  }

  class MessageHelper(msgHeader: MessageHeader) {
    def to(rcv: Name) = msgHeader._1.name send(new Message(rcv,msgHeader._2))
  }

}
