package main.scala

import bigactors.{Observation, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR


object TestRemoteBigActors extends App{

 val uav1 = new RemoteBigActor( Symbol("uav1"), Symbol("u1")){
    def behavior() {
      control(new BRR("u1_UAV[network].$0 | $1 -> u1_UAV[network].($0 | uav1_BA) | $1"))
      observe("children.parent.host")
      loop {
        react {
          case obs: Observation => println("New observation for uav1: " + obs)
          case msg: Any => println("New mail for uav1: " + msg)
        }
      }
    }
  }


  val uav0 = new RemoteBigActor( Symbol("uav0"), Symbol("u0")){
    def behavior() {
      control(new BRR("u0_UAV[network].$0 | $1 -> u0_UAV[network].($0 | uav0_BA) | $1"))
      observe("children.linkedTo.host")
      Thread.sleep(5000)
      react{
        case obs: Observation => {
          println("New observation for uav0: "+ obs)
          obs.bigraph.foreach(b =>
            sendMsg("Hello I'm BigActor " + bigActorID,Symbol(b.toString))
          )
          control(new BRR("l0_Location.(u0_UAV[network].$0 | $1) | l1_Location.$2 -> l0_Location.($1) | l1_Location.(u0_UAV[network].$0 | $2)"))
          migrate(Symbol("u1"))
          observe("host")
          react{
            case obs: Observation => println("New observation for uav0: "+ obs)
          }
        }
      }
    }
  }

  uav1.start
  uav0.start

}
