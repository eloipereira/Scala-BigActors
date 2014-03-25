package main.scala

import bigactors._
import edu.berkeley.eloi.bigraph.BRR
import bigactors.BigActor._
import scala.actors.Actor._

object TestBigActors extends App{
  BigraphManager
  BigActorSchdl

  val uav1 = BigActor hosted_at "u1" with_behavior{
    observe("children.parent.host")
    loop {
      react {
        case obs: Observation => println("New observation for uav1: " + obs)
        case msg: Any => println("New mail for uav1: " + msg)
      }
    }
  }

  val uav0 = bigActor(Symbol("u0")){
    observe("children.parent.host")
    react{
      case obs: Observation => {
        println("New observation for uav0: "+ obs)
        sendMsg("Hello I'm a BigActor!",uav1)
        Thread.sleep(1000)
        control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
        migrate(Symbol("u1"))
        observe("host")
        react{
          case obs: Observation => println("New observation for uav0: "+ obs)
        }
      }
    }
  }

}
