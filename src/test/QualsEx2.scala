package test

import bigactors._
import bigactors.BigActorImplicits._
import actors.Actor._

object QualsEx2 extends App{

  new BigActor("NomadicObserver","camera0") {
    def act() {
      loop {
        observe("children.parent.host")
        react {
          case obs: Observation => {
            println(obs)
            if (obs.contains("p0") || obs.contains("p1") || obs.contains("p2")){
              println(obs)
            } else{
              observe("linkedTo.host")
              react {
                case obs1: Observation => migrate(obs1.obs.last.getId)
              }
            }
          }
        }
      }
    }
  }

}
