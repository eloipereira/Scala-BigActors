package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._

abstract class BigActor(val bigActorID: BigActorID, var hostId: HostID) extends Actor{

  private var bigraphSchdl: Actor = null

  def setScheduler (bigraphSchdl: Actor) {
    this.bigraphSchdl = bigraphSchdl
    bigraphSchdl ! ("HOSTING", hostId,bigActorID)
  }

  def observe(query: String) {
    bigraphSchdl ! ("OBSERVE",query, hostId)
  }

  def control(u: BRR) {
    bigraphSchdl ! ("CONTROL",u,hostId)
  }

  def migrate(newHostId: HostID) {
    bigraphSchdl ! ("MIGRATE",hostId,newHostId, bigActorID)
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
  def toString: String =  bigActorID + " hosted_at " + hostId
}


object BigActor {
  def bigActor(id: BigActorID)(hostId: HostID)(body: => Unit): BigActor = {
    val b = new BigActor(id,hostId) {
      def act() = body
    }
    b
  }
}