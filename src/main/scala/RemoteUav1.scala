package bigactors
package remote

import edu.berkeley.eloi.bigraph.Place


object RemoteUav1 extends RemoteBigActor(Symbol("uav1"), Symbol("u1")) with App{
  def behavior() {
    observe("children.parent.host")
    loop {
      react {
        case obs: Array[Place] => println("New observation for uav1: " + obs)
        case msg: Any => println("New mail for uav1: " + msg)
      }
    }
  }
  start
}




