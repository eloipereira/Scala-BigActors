package bigactors

import scala.actors.Actor
import Actor._
import edu.berkeley.eloi.bigraph._

abstract class BigActor (val name: String, host0: Node, bigraphSchdl: Actor) extends Actor{
  private var host: Node = host0

  def observe(q: Query) {
    bigraphSchdl ! q
  }
  def control(u: BRR) {
    bigraphSchdl ! u
  }
  def migrate(h: Node) {
    val tmpActor = actor {
      bigraphSchdl ! (h,host)
      self.receive {
        case b: Boolean => if (b) {
          this.host = h
          println("Migration succeeded")
          exit()
        } else System.err.println("Migration failed.")
      }
    }
  }

  def send(dest: BigActor, message: Any) {
    bigraphSchdl ! new Message(dest, message)
  }

  def getHost: Node = host

  override
  def toString: String =  name + "@" + host
}
