package test

import bigactors._
import scala.actors._
import edu.berkeley.eloi.bigraph._
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler
import scala.collection.JavaConversions._

object TestBigActors {
  class SimpleBigActor0(name: String,  hostId: String, nodeId: String, move: BRR, buddy: BigActor, bigraphSchdl: Actor) extends BigActor(name, hostId, bigraphSchdl){
    def act(){
      this.!(buddy, "Hello buddy!")
      observe(new Query)
      migrate(nodeId)
      control(move)
    }
  }

  class SimpleBigActor1(name: String,  hostId: String, bigraphSchdl: Actor) extends BigActor(name, hostId, bigraphSchdl){
    def act(){
      loop {
        react{
          case s: String => println("Just got some mail: " + s)
        }
      }
    }
  }

  def main(args: Array[String]){
    val gen = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true)
    val brs: BRS = new BRS(gen.signature, gen.names, gen.bigraph, gen.rules)
    println(brs.getBigraph.getNodes)
    val scheduler = new BigraphSchdl(brs)
    val ba1 = new SimpleBigActor1("uav1","u1", scheduler)
    val ba0 = new SimpleBigActor0("uav0", "u0", "u1", brs.getRules().get(0), ba1, scheduler)
    scheduler.start()
    ba0.start()
    ba1.start()
  }
}
