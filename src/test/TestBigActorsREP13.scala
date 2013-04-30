package test

import bigactors._
import edu.berkeley.eloi.bigraph._

object TestBigActorsREP13 extends App{

  new BigActor(new BigActorID("uav0"),new HostID("u0")){
    def act() {
      var tankerNotFound = true
      control(new BigraphReactionRule("airfield_Location.(u0_UAV[wifi] | $0) | searchArea_Location.$1 -> airfield_Location.$0 | searchArea_Location.(u0_UAV[wifi] |$1)"))
      while (tankerNotFound){
        observe("children.parent.host")
        react {
          case obs: Observation =>
            if (obs.contains(new Node("tanker0"))) {
              send(new Message(BigActorID("uav0"),BigActorID("cs0"),obs))
              control(new BigraphReactionRule("airfield_Location.$0 | searchArea_Location.(u0_UAV[x] | $1) -> airfield_Location.(u0_UAV[x] | $0) | searchArea_Location.$1"))
              tankerNotFound = false
            }
        }
      }
    }
  }

  new BigActor(new BigActorID("cs0"), new HostID("cs0")) {
    def act() {
      react{
        case msg: Message => {
          control(new BigraphReactionRule("vessel0_Vessel[x,ais].$0 || cs0_ControlStation[wifi] -> vessel0_Vessel[wifi,ais].$0 || cs0_ControlStation[wifi]"))
          send(new Message(BigActorID("cs0"),BigActorID("vessel0"),msg.message))
        }
      }
    }
  }

  new BigActor(new BigActorID("vessel0"), new HostID("vessel0")) {
    def act() {
      react{
        case msg: Message => {
          control(new BigraphReactionRule("$0|harbour_Location.(vessel0_Vessel[x,y].$2| $1) | searchArea_Location.$3 -> $0|harbour_Location.$1 | searchArea_Location.(vessel0_Vessel[x,y].$2|$3)"))
          control(new BigraphReactionRule("vessel0_Vessel[x,y].(drifter0_Drifter[y]| $1) -> vessel0_Vessel[x,y].$1|drifter0_Drifter[y]"))
          control(new BigraphReactionRule("vessel0_Vessel[x,y].(drifter1_Drifter[y]| $1) -> vessel0_Vessel[x,y].$1|drifter1_Drifter[y]"))
        }
      }
    }
  }

  new BigActor(new BigActorID("env0"), new HostID("searchArea")) {
    def act() {
      control(new BigraphReactionRule("searchArea_Location.$0 -> searchArea_Location.(tanker0_Tanker|$0)"))
    }
  }
}
