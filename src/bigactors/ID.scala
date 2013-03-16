package bigactors

import edu.berkeley.eloi.bigraph.{Node, BRR}

abstract class ID
case class HostID (name: String) extends ID {
  if (!Initializer.scheduler.getBRS.getBigraph.getNodes.contains(new Node(name))){
    System.err.println("Invalid host: "+ name + " is not a node in the current bigraph.")
    System.exit(0)
  }

  override def toString = name
}

case class BigActorID (name: String) extends ID {

  def getName = name

  override def toString = name
}

