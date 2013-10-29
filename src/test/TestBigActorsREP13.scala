package test

import bigactors._
import edu.berkeley.eloi.bigraph.{BRR, BigraphNode}


object TestBigActorsREP13 extends App{

val ba0 = new BigActor( Symbol("uav0"), Symbol("u0")){
    def act() {
      var tankerNotFound = true
      control(new BRR("airfield_Location.(u0_UAV[wifi] | $0) | searchArea_Location.$1 -> airfield_Location.$0 | searchArea_Location.(u0_UAV[wifi] |$1)"))
      while (tankerNotFound){
        observe("children.parent.host")
        react {
          case obs: Observation =>
            if (obs.contains(new BigraphNode("tanker0"))) {
              send(new Message(Symbol("cs0"),obs))
              control(new BRR("airfield_Location.$0 | searchArea_Location.(u0_UAV[x] | $1) -> airfield_Location.(u0_UAV[x] | $0) | searchArea_Location.$1"))
              tankerNotFound = false
            }
        }
      }
    }
  }

  new BigActor( Symbol("cs0"),  Symbol("cs0")) {
    def act() {
      react{
        case msg: Message => {
          control(new BRR("vessel0_Vessel[x,ais].$0 || cs0_ControlStation[wifi] -> vessel0_Vessel[wifi,ais].$0 || cs0_ControlStation[wifi]"))
          send(new Message(Symbol("vessel0"),msg.message))
        }
      }
    }
  }

  new BigActor( Symbol("vessel0"),  Symbol("vessel0")) {
    def act() {
      react{
        case msg: Message => {
          control(new BRR("$0|harbour_Location.(vessel0_Vessel[x,y].$2| $1) | searchArea_Location.$3 -> $0|harbour_Location.$1 | searchArea_Location.(vessel0_Vessel[x,y].$2|$3)"))
          control(new BRR("vessel0_Vessel[x,y].(drifter0_Drifter[y]| $1) -> vessel0_Vessel[x,y].$1|drifter0_Drifter[y]"))
          control(new BRR("vessel0_Vessel[x,y].(drifter1_Drifter[y]| $1) -> vessel0_Vessel[x,y].$1|drifter1_Drifter[y]"))
        }
      }
    }
  }

  new BigActor( Symbol("env0"),  Symbol("searchArea")) {
    def act() {
      control(new BRR("searchArea_Location.$0 -> searchArea_Location.(tanker0_Tanker|$0)"))
    }
  }
}
