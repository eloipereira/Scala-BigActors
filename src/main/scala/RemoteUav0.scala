import bigactors.{Observation, Message, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR
;

object RemoteUav1 extends RemoteBigActor( Symbol("uav1"), Symbol("u1")) with App{
  def behavior() {
    observe("children.parent.host")
    loop {
      react {
        case msg: Message => println("New mail for uav1: " + msg.message)
        case obs: Observation => println("New observation for uav1: " + obs)
      }
    }
  }
  start
}