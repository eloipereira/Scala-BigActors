package bigactors

import scala.actors.Actor
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
    bigraphSchdl ! h
    react {
      case true => {host = h
        println("Migration succeeded")
      }
      case false => System.err.println("Migration failed.")
    }
  }
  def send(dest: BigActor, message: Any) {
    bigraphSchdl ! new Message(dest, message)
  }

  def getHost: Node = host

  override
  def toString: String =  name + "@" + host
}
