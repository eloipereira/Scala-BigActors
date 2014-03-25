package bigactors
package remote

import bigactors.Observation
import RemoteBigActorImplicits._
import edu.berkeley.eloi.bigraph.BRR

object TestRemoteBigActorsNoName extends App{

  val uav1 = new RemoteBigActor(Symbol("u1")){
     def behavior() {
       control(new BRR(hostID.name + "_UAV[network].$0 | $1 -> " + hostID.name +"_UAV[network].($0 | " + bigActorID.name + "_BA) | $1"))
       observe("children.parent.host")
       loop {
         react {
           case obs: Observation => println("New observation for BigActor " + bigActorID.name + ": " + obs)
           case msg: Any => println("New mail for BigActor " + bigActorID.name + ": " + msg)
         }
       }
     }
   }


   val uav0 = new RemoteBigActor(Symbol("u0")){
     def behavior() {
       control(new BRR(hostID.name + "_UAV[network].$0 | $1 -> " + hostID.name + "_UAV[network].($0 | " + bigActorID.name + "_BA) | $1"))
       observe("children.linkedTo.host")
       Thread.sleep(5000)
       react{
         case obs: Observation => {
           println("New observation for uav0: "+ obs)
           obs.bigraph.foreach(b =>
             sendMsg("Hello I'm BigActor " + bigActorID.name ,Symbol(b.toString))
           )
           control(new BRR("l0_Location.(u0_UAV[network].$0 | $1) | l1_Location.$2 -> l0_Location.($1) | l1_Location.(u0_UAV[network].$0 | $2)"))
           migrate(Symbol("u1"))
           observe("host")
           react{
             case obs: Observation => println("New observation for BigActor " + bigActorID.name + ": " + obs)
           }
         }
       }
     }
   }

   uav1.start
   uav0.start
}
