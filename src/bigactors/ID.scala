package bigactors

abstract class ID
case class HostID (name: String) extends ID {
  override def toString = name
}

case class BigActorID (name: String) extends ID {

  def getName = name

  override def toString = name
}
