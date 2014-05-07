package bigactors

import bigactors._
import edu.berkeley.eloi.bigraph.{BigraphNode, BRR}
import bigactors.BigActor._
import scala.actors.Actor._

object TestBigActors extends App{
  BigraphManager
  BigActorSchdl

  val uav1 = BigActor hosted_at "u1" with_behavior{
    observe("children.parent.host")
    loop {
      react {
        case observation: Observation => {
          if (observation contains "u0") println("I observed u0")

        }
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
        control(new BRR("l0_Location.(u0_UAV[network] | $0) | l1_Location.$1 -> l0_Location.$0 | l1_Location.(u0_UAV[network] | $1)"))
        migrate(Symbol("u1"))
        observe("host")
        react{
          case obs: Observation => println("New observation for uav0: "+ obs)
        }
      }
    }
  }

}
