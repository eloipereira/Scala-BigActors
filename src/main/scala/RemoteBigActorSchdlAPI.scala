package bigactors
import edu.berkeley.eloi.bigraph.BRR

sealed trait RemoteBigActorSchdlAPI extends Serializable
case class REMOTE_HOSTING_REQUEST(bigActorID: Symbol, ip: String, port: Int, hostId: Symbol) extends RemoteBigActorSchdlAPI
case object REMOTE_HOSTING_SUCCESSFUL extends RemoteBigActorSchdlAPI
case class REMOTE_OBSERVATION_REQUEST(query: String, bigActorID: Symbol) extends RemoteBigActorSchdlAPI
case class REMOTE_CONTROL_REQUEST(brr: BRR, bigActorID: Symbol) extends RemoteBigActorSchdlAPI
case class REMOTE_MIGRATION_REQUEST(newHostId: Symbol, bigActorID: Symbol) extends RemoteBigActorSchdlAPI
case class REMOTE_SEND_REQUEST(val msg: Any, rcvID: Symbol , val bigActorID: Symbol) extends RemoteBigActorSchdlAPI
//case class REMOTE_SEND_REQUEST(val msg: Message, val bigActorID: Symbol) extends RemoteBigActorSchdlAPI