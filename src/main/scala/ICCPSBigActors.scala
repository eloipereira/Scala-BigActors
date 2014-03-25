package bigactors
package remote

import RemoteBigActorImplicits._
import scala.actors.Actor._


object ICCPSBigActors extends App{

  "searchOil" hosted_at "uav0" with_behavior {
    "searchOil" observe "children.parent.host"
    loop {
      react {
        case obs: Observation => {
          if (obs.contains("oilSpill")){
            "searchOil" control "uav0_UAV[c2,ais] || oilSpill_Location.($0)| $1 -> oilSpill_Location.($0 | uav0_UAV[c2,ais]) | $1"
            "searchOil" send_message obs to "deployDrifter"
          }
          else{
            if (obs.contains("drifter")){
              "searchOil" control "uav0_UAV[c2,ais] || drifter_Drifter[ais].($0)| $1 -> drifter_Drifter[ais].($0 | uav0_UAV[c2,ais])| $1"
            } else{
              "searchOil" observe "children.parent.host"
            }
          }
        }
      }
    }
  }

  "deployDrifter" hosted_at "vessel" with_behavior
    {
      react{
        case msg: Observation => {
          "deployDrifter" control "vessel_Vessel[c2].(drifter_Drifter[ais]|$0) || oilSpill_Location.($1) | $2 -> vessel_Vessel[c2].($0) || oilSpill_Location.($1|drifter_Drifter[ais]) | $2"
        }
      }
    }
}


trait ICCPSReactionRules{
//  def MOVE_VEHICLE_TO(uav: BigraphNode)(destination: BigraphNode): BRR = {
//
//
//  }
}