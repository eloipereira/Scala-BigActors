import bigactors.{Observation, Message, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR


object TestRemoteBigActorsNoName extends App{

 val uav1 = new RemoteBigActor(Symbol("u1")){
    def behavior() {
      observe("children.parent.host")
      loop {
        react {
          case obs: Observation => println("New observation for uav1: " + obs)
        }
      }
    }
  }
  uav1.start

}
