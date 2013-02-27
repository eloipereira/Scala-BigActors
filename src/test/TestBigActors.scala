package test

import actors.Actor._
import bigactors.BigActorImplicits._
import edu.berkeley.eloi.bigraph._
import bigactors._


object TestBigActors extends App{

  val ba1 = new BigActor(BigActorID("uav1"),HostID("u1")){
    def act() {
      observe("children.parent.host")
      loop {
        react {
          case msg: Message => println("New mail for uav1: " + msg.message)
          case obs: Observation => println("New observation for uav1: " + obs)
        }
      }
    }
  }


  val ba0 = new BigActor(BigActorID("uav0"),HostID("u0")){
    def act() {
      observe("children.parent.host")
      react{
        case obs: Observation => {
          println("New observation for uav0: "+ obs)
          send(new Message(BigActorID("uav0"),BigActorID("uav1"),"Hello I'm a BigActor!"))
          control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
          migrate(HostID("u1"))
          observe("host")
          react {
            case obs: Observation => println("New observation for uav0: "+ obs)
          }
        }
      }
    }
  }
}
