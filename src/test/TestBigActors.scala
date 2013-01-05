package test

import bigactors._
import scala.actors._
import edu.berkeley.eloi.bigraph._
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler
import scala.collection.JavaConversions._

object TestBigActors {

  class SimpleBigActor0(name: String, host: Node, node: Node, move: BRR, buddy: BigActor, bigraphSchdl: Actor) extends BigActor(name, host, bigraphSchdl){
    def act(){
      this.!(buddy, "Hello buddy!")
      observe(new Query)
      migrate(node)
      control(move)
    }
  }

  class SimpleBigActor1(name: String, host: Node, bigraphSchdl: Actor) extends BigActor(name, host, bigraphSchdl){
    def act(){
      loop {
        react{
          case s: String => println("Just got some mail: " + s)
        }
      }
    }
  }

  def main(args: Array[String]){
    val brs: BRS = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm")
    val scheduler = new BigraphSchdl(brs)
    val ba1 = new SimpleBigActor1("uav1",brs.nodes.get(2), scheduler)
    val ba0 = new SimpleBigActor0("uav0", brs.nodes.get(1), brs.nodes.get(2), brs.rules.get(0), ba1, scheduler)
    scheduler.start()
    ba0.start()
    ba1.start()
  }
}
