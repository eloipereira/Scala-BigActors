package test

import bigactors._
import edu.berkeley.eloi.bigraph._
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler

object TestBigActors2 {

  def main(args: Array[String]){
    val brs: BRS = new BRS("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true)
    val scheduler = new BigraphSchdl(brs)
    scheduler.start()

    val ba1 = new BigActor(HostID("u1")) {
      def act(){
        loop {
          react{
            case m: Any => println("Just got some mail: " + m)
          }
        }
      }
    }
    ba1.setScheduler(scheduler)
    ba1.start()


    val ba0 = new BigActor(HostID("u0")) {
      def act(){
        observe("children.parent.host")
        react{
          case o: Observation => {
            println("Observation arrived: "+ o.toString)
            ba1 ! (new Message(this,ba1,"Hello I'm a BigActor!"))
            control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
            migrate(HostID("u1"))
          }
        }
      }
    }
    ba0.setScheduler(scheduler)
    ba0.start()
  }
}
