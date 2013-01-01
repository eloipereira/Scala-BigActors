package bigactors

import scala.actors.Actor
import edu.berkeley.eloi.bigraph._

abstract class BigActor (host0: Node, bigraphSchdl: Actor) extends Actor{
  var host: Node = host0

  def observe(q: Query) {
    bigraphSchdl ! q
  }
  def control(u: BRR) {
    bigraphSchdl ! u
  }
  def migrate(h: Node) {
    bigraphSchdl ! h
  }
  //def send(dest: BigActor, message: Message) {
  //  Snd(host,dest,message) ! bigraphSchdl
  //}
}
