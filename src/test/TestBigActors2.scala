package test

import bigactors._
import scala.actors._
import edu.berkeley.eloi.bigraph._
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler

object TestBigActors2 {

  def main(args: Array[String]){
    val gen = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true)
    val brs: BRS = new BRS(gen.signature, gen.names, gen.bigraph, gen.rules)
    val scheduler = new BigraphSchdl(brs)
    scheduler.start()


    object ba1 extends BigActor(new HostID("u1")) {
      def act(){
        loop {
          react{
            case m: Message => println("Just got some mail: " + m)
            case s: String => println("Just got another mail: "+ s)
          }
        }
      }
    }
    ba1.setScheduler(scheduler)


    //Thread.sleep(2000)


    object ba0 extends BigActor(new HostID("u0")) {
      def act(){
        observe("children.parent.host")
        //send(new Message(ba0,ba1,"Hello I'm a BigActor!"))
        ba1 ! (new Message(ba0,ba1,"Hello I'm a BigActor!"))
        //ba1 ! ("Hello I'm a BigActor!")
        migrate(new HostID("u1"))
        control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
      }
    }
    ba0.setScheduler(scheduler)

    ba1.start()
    ba0.start()


    object act0 extends Actor{
      def act(){
        ba1 ! "I'm an Actor"
      }
    }
    act0.start()
  }
}
