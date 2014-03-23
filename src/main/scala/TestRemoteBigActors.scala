package main.scala

import bigactors.{Observation, Message, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR


object TestRemoteBigActors extends App{

 val uav1 = new RemoteBigActor( Symbol("uav1"), Symbol("u1")){
    def behavior() {
      observe("children.parent.host")
      loop {
        react {
          case msg: Message => println("New mail for uav1: " + msg.message)
          case obs: Observation => println("New observation for uav1: " + obs)
        }
      }
    }
  }
  uav1.start

  val uav0 = new RemoteBigActor( Symbol("uav0"), Symbol("u0")){
    def behavior() {
      observe("children.parent.host")
      react{
        case obs: Observation => {
          println("New observation for uav0: "+ obs)
          send(new Message(Symbol("uav1"),"Hello I'm a BigActor!"))
          Thread.sleep(5000)
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
  uav0.start

}
