package bigactors

import _root_.akka.actor.ActorRef
import edu.berkeley.eloi.bigraph.BRR
import scala.actors.OutputChannel

sealed trait BigActorSchdlAPI
case class HOSTING_REQUEST(hostId: Symbol) extends BigActorSchdlAPI
case class HOSTING_REQUEST_AKKA(hostId: Symbol, actor: ActorRef) extends BigActorSchdlAPI
case object HOSTING_SUCCESSFUL extends BigActorSchdlAPI
case object HOSTING_UNSUCCESSFUL extends BigActorSchdlAPI
case class OBSERVATION_REQUEST(query: Query) extends BigActorSchdlAPI
case class OBSERVATION_REQUEST_AKKA(query: Query, hostId: Symbol) extends BigActorSchdlAPI
case class CONTROL_REQUEST(brr: BRR) extends BigActorSchdlAPI
case class CONTROL_REQUEST_AKKA(brr: BRR, hostId:Symbol) extends BigActorSchdlAPI
case class MIGRATION_REQUEST(newHostId: Symbol) extends BigActorSchdlAPI
case class MIGRATION_REQUEST_AKKA(newHostId: Symbol, hostId: Symbol) extends BigActorSchdlAPI
case class SEND_REQUEST(msg: Any, rcv:OutputChannel[Any]) extends BigActorSchdlAPI
case class SEND_REQUEST_AKKA(msg: Any, rcv:ActorRef, hostId:Symbol) extends BigActorSchdlAPI
case object REQUEST_HOSTING_RELATION extends BigActorSchdlAPI
