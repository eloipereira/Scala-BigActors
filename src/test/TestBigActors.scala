package test

import bigactors._
import scala.actors._
import edu.berkeley.eloi.bigraph._
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler

object TestBigActors {

  class SimpleBigActor(host: Node, bigraphSchdl: Actor) extends BigActor(host, bigraphSchdl){
    def act(){
        observe(new Query)
        control(new BRR("MOVE","l0_Location[x].(u0_UAV | $0) | l1_Location[x].$1","l0_Location[x].$0 | l1_Location[x].(u0_UAV | $1)"))
      }
  }

  def main(args: Array[String]){

    val brs: BRS = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm")
    println(brs)
    val scheduler = new BigraphSchdl(brs)
    val ba0 = new SimpleBigActor(brs.nodes.get(0), scheduler)
    scheduler.start()
    ba0.start()
  }
}
