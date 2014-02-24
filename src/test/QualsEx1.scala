package test

import bigactors._
import bigactors.RemoteBigActorImplicits._

object QualsEx1 extends App{

  new RemoteBigActor("observer","camera0") {
    def behavior() {
      loop {
        observe("children.parent.host")
        react {
          case obs: Observation => println(obs)
        }
      }
    }
  }

  new RemoteBigActor("env","room0"){
    def behavior(){
      control("room0_Room.$0 -> room0_Room.(p0_Person|$0)")
      control("room0_Room.$0 -> room0_Room.(p1_Person|$0)")
      control("room0_Room.$0 -> room0_Room.(p2_Person|$0)")
    }
  }
}
