package bigactors

import edu.berkeley.eloi.bigraph.BRR

abstract class ID
case class HostID (name: String) extends ID {
  override def toString = name
}
case class BigActorID (name: String) extends ID {

  def observe(query: String) {
      Initializer.scheduler ! ("OBSERVE",query, this)
    }

    def control(u: BRR) {
      Initializer.scheduler ! ("CONTROL",u, this)
    }

    def migrate(newHostId: HostID) {
      Initializer.scheduler ! ("MIGRATE", this,newHostId)
    }

    def send(msg: Message){
      Initializer.scheduler ! ("SEND",msg)
    }

  override def toString = name
}

