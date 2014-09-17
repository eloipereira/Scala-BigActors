package bigactors.remote.examples

import bigactors.remote.RemoteBigActor
import bigactors.{Children, Host, Parent}
import edu.berkeley.eloi.bigraph.Place


object RemoteUav1 extends RemoteBigActor(Symbol("uav1"), Symbol("u1")) with App{
  def behavior() {
    observe(Children(Parent(Host)))
    loop {
      react {
        case obs: Array[Place] => println("New observation for uav1: " + obs)
        case msg: Any => println("New mail for uav1: " + msg)
      }
    }
  }
  start
}




