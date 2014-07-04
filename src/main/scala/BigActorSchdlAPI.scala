package bigactors

import _root_.akka.actor.ActorRef
import edu.berkeley.eloi.bigraph.BRR
import scala.actors.OutputChannel

sealed trait BigActorSchdlAPI
case class HOSTING_REQUEST(hostId: Symbol) extends BigActorSchdlAPI
case object HOSTING_SUCCESSFUL extends BigActorSchdlAPI
case class OBSERVATION_REQUEST(query: Query) extends BigActorSchdlAPI
case class CONTROL_REQUEST(brr: BRR) extends BigActorSchdlAPI
case class MIGRATION_REQUEST(newHostId: Symbol) extends BigActorSchdlAPI
case class SEND_REQUEST(msg: Any, rcv:OutputChannel[Any]) extends BigActorSchdlAPI
case class SEND_REQUEST_AKKA(msg: Any, rcv:ActorRef) extends BigActorSchdlAPI
case object REQUEST_HOSTING_RELATION extends BigActorSchdlAPI