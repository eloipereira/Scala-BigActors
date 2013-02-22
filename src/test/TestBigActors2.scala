package test

import actors.Actor._
import bigactors._
import BigActor._
import BigActorImplicits._
import edu.berkeley.eloi.bigraph._

object TestBigActors2 {

  def main(args: Array[String]){
    val brs: BRS = new BRS("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true)
    val scheduler = new BigraphSchdl(brs)
    scheduler.start()


    val ba1 = "uav1" hosted_at "u1" with_behavior
      {
        loop {
          react{
            case m: Any => println("Just got some mail: " + m)
          }
        }
      }

    ba1.setScheduler(scheduler)
    ba1.start()
    println(ba1)

    val ba0 = new BigActor(BigActorID("uav0"),HostID("u0")) {
      def act(){
        observe("children.parent.host")
        react{
          case o: Observation => {
            println("Observation arrived: "+ o.toString)
            ba1 ! (new Message(this,ba1,"Hello I'm a BigActor!"))
            control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
            migrate("u1")
          }
        }
      }
    }
    ba0.setScheduler(scheduler)
    ba0.start()
  }
}
