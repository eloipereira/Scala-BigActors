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


    object ba1 extends BigActor(new HostID("u1"), scheduler) {
      def act(){
        loop {
          react{
            case s: Any => println("Just got some mail: " + s.toString)
          }
        }
      }
    }
    ba1.start()

    object ba0 extends BigActor(new HostID("u0"), scheduler) {
      def act(){
        observe("children.parent.host", this)
        ba1 ! "Hello I'm a BigActor!"
        migrate(new HostID("u1"))
        control(brs.getRules().get(0))
      }
    }
    ba0.start()

    object act0 extends Actor{
      def act(){
        ba1 ! "Hey, I'm an Actor"
      }
    }
    act0.start()
  }
}
