package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import bigactors.BigActor._

abstract class BigActor(val bigActorID: BigActorID, val initialHostId: HostID) extends Actor {

  Initializer.scheduler ! ("HOSTING", initialHostId,bigActorID, this)

  def observe(query: String) {
    Initializer.scheduler ! ("OBSERVE",query, bigActorID)
  }

  def control(u: BRR) {
    Initializer.scheduler ! ("CONTROL",u, bigActorID)
  }

  def migrate(newHostId: HostID) {
    Initializer.scheduler ! ("MIGRATE", bigActorID,newHostId)
  }

  def send(msg: Message){
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


object BigActor {
  def bigActor(id: BigActorID)(hostId: HostID)(body: => Unit): BigActor = {
    val b = new BigActor(id,hostId) {
      def act() =  body
    }
    b
  }
}


object BigActorImplicits {
  type BigActorSignature = (BigActorID, HostID)
  type Name = String

  implicit def Name2HostID(name: Name) = HostID(name)
  implicit def Name2BigActorID(name: Name) = BigActorID(name)
  implicit def Name2BigActorIDHelper(bigActorName: Name) = new BigActorIDHelper(bigActorName)
  implicit def BigActorSignature2BigActorSignatureHelper(signature: BigActorSignature) = new  BigActorSignatureHelper(signature)

  class BigActorIDHelper(bigActorName: Name){
    def hosted_at(hostName:Name): BigActorSignature = (BigActorID(bigActorName),HostID(hostName))
  }

  class BigActorSignatureHelper(signature: BigActorSignature){
    def with_behavior(body: => Unit): BigActor = bigActor(signature._1)(signature._2)(body)
  }
}
