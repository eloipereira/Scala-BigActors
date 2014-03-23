package bigactors

import edu.berkeley.eloi.bigraph.BRR
import scala.actors.OutputChannel

sealed trait BigActorSchdlAPI
case class HOSTING_REQUEST(hostId: Symbol) extends BigActorSchdlAPI
case object HOSTING_SUCCESSFUL extends BigActorSchdlAPI
case class OBSERVATION_REQUEST(query: String) extends BigActorSchdlAPI
case class CONTROL_REQUEST(brr: BRR) extends BigActorSchdlAPI
case class MIGRATION_REQUEST(newHostId: Symbol) extends BigActorSchdlAPI
case class SEND_REQUEST(msg: Any, rcv:OutputChannel[Any]) extends BigActorSchdlAPI
