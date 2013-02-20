package bigactors

abstract class ID
case class HostID (name: String) extends {

  override
  def toString: String = name
}

object HostID{

  implicit def string2HostID(name: String) = new HostID(name)
}

