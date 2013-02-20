package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._

abstract class BigActor(var hostId: HostID) extends Actor{

  private var bigraphSchdl: Actor = null

  def setScheduler (bigraphSchdl: Actor) {
    this.bigraphSchdl = bigraphSchdl
    bigraphSchdl ! ("HOSTING", hostId)
  }

  def observe(query: String) {
    bigraphSchdl ! ("OBSERVE",query, hostId)
  }

  def control(u: BRR) {
    bigraphSchdl ! ("CONTROL",u,hostId)
  }

  def migrate(newHostId: HostID) {
    bigraphSchdl ! ("MIGRATE",hostId,newHostId)
    this.hostId = newHostId
  }

  override def !(msg:Any){
    msg match {
      case x@(m: Message) => bigraphSchdl ! ("SEND", m, m.receiver)
      case x@("SEND_SUCCESSFUL",m:Message) => super.!(m)
      case x@("OBSERVATION_SUCCESSFUL",o:Observation) => super.!(o)
      case _ =>
    }
  }

  def getHostId: HostID = {
    this.hostId
  }

  override
  def toString: String =  "Bigactor @" + hostId
}


//object BigActor {
//  def bigActor (host: HostID) = (body: => Unit) => {
//    val b = new BigActor(host) {
//      def act() = body
//    }
//    b
//  }
//
//  def hostedAt = (name:String) => new HostID(name)
//
//  def withBehavior (body: => Unit) {
//    body
//  }
//}