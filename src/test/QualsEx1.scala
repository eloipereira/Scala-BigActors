package test

import bigactors._
import bigactors.BigActorImplicits._
import actors.Actor._

object QualsEx1 extends App{

  new BigActor("observer","camera0") {
    def act() {
      loop {
        observe("children.parent.host")
        react {
          case obs: Observation => send(new Message("observer","server",obs))
        }
      }
    }
  }

  "server" hosted_at "srv0" with_behavior{
    loop {
      react{
        case x: Any => println(x)
      }
    }

  }

}
