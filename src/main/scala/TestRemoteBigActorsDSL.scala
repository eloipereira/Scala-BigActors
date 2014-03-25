package bigactors
package remote

import bigactors._
import RemoteBigActorImplicits._
import scala.actors.Actor._

object TestRemoteBigActorsDSL extends App{
 // BigActorSchdl.start
 // BigraphManager.start


  "uav1" hosted_at "u1" with_behavior {
    "uav1" observe "children.parent.host"
    loop {
      react {
        case obs: Observation => println("New observation for uav1: " + obs)
        case msg: Any => println("New message for uav1: " + msg)
      }
    }
  }

  "uav0" hosted_at "u0" with_behavior
    {
      "uav0" observe "children.parent.host"
      react{
        case obs: Observation => {
          println("New observation for uav0: "+ obs)
          "uav0" send_message "Hello I'm a BigActor!" to "uav1"
          "uav0" control "l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"
          "uav0" migrate "u1"
          "uav0" observe "host"
          receive {
            case obs: Observation => println("New observation for uav0: "+ obs)
          }
          "uav0" control "l0_Location[x].(u1_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u1_UAV[z] | $1)"
          "uav0" observe "children.parent.host"
          receive {
            case obs: Observation => println("New observation for uav0: "+ obs)
          }
        }
      }
    }
}
