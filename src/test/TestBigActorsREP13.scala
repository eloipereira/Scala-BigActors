package test

import bigactors._


object TestBigActorsREP13 extends App{

  new BigActor(new BigActorID("uav0"),new HostID("u0")){
    def act() {
       control(new BigraphReactionRule("airfield_Location.(u0_UAV[wifi] | $0) | searchArea_Location.$1 -> airfield_Location.$0 | searchArea_Location.(u0_UAV[wifi] |$1)"))
    }
  }

}
