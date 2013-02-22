package bigactors

abstract class ID
case class HostID (name: String) extends ID {
  override def toString = name
}
case class BigActorID (name: String) extends ID {
  override def toString = name
}

