import bigactors.{Observation, Message, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR


object TestRemoteBigActorsNoName extends App{

 val uav0 = new RemoteBigActor(Symbol("u0")){
    def behavior() {
      observe("children.parent.host")
      loop {
        react {
          case obs: Observation => println("New observation from " + bigActorID + ": " + obs)
        }
      }
    }
  }
  uav0.start

  val uav1 = new RemoteBigActor(Symbol("u1")){
     def behavior() {
       observe("children.parent.host")
       loop {
         react {
           case obs: Observation => println("New observation from " + bigActorID + ": " + obs)
         }
       }
     }
   }
  uav1.start
}
