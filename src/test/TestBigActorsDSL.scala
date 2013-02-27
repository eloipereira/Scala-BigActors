package test

import actors.Actor._
import bigactors.BigActorImplicits._
import edu.berkeley.eloi.bigraph._
import bigactors._
import bigactors.BigActor._


object TestBigActorsDSL extends App{

  "uav1" hosted_at "u1" with_behavior
    {
      "uav1" observe "children.parent.host"
      loop {
        react {
          case msg: Message => println("New mail for uav1: " + msg.message)
          case obs: Observation => println("New observation for uav1: " + obs)
        }
      }
    }


  "uav0" hosted_at "u0" with_behavior
    {
      "uav0" observe "children.parent.host"
      react{
        case obs: Observation => {
          println("New observation for uav0: "+ obs)
          "uav0" send new Message("uav0","uav1","Hello I'm a BigActor!")
          "uav0" control new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)")
          "uav0" migrate "u1"
          "uav0" observe "host"
          react {
            case obs: Observation => println("New observation for uav0: "+ obs)
          }
        }
      }
    }
}
