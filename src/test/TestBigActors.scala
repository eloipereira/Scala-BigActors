package test

import bigactors._
import scala.actors._
import edu.berkeley.eloi.bigraph._
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler
import scala.collection.JavaConversions._

object TestBigActors {

  class SimpleBigActor0(name: String, host: Node, move: BRR, mate: BigActor, bigraphSchdl: Actor) extends BigActor(name, host, bigraphSchdl){
    def act(){
      send(mate,"Hello buddy!")
      observe(new Query)
      control(move)
    }
  }

  class SimpleBigActor1(name: String, host: Node, bigraphSchdl: Actor) extends BigActor(name, host, bigraphSchdl){
    def act(){
      loop {
        react{
          case s: String => println("Hey " + sender)
        }
      }
    }
  }

  def main(args: Array[String]){
    val brs: BRS = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm")
    val scheduler = new BigraphSchdl(brs)
    println(brs.bigraph)
    println(brs.signature)
    println(brs.nodes) //TODO - fix the nodes list in bgm2java
    println(brs.names)
    println(brs.rules)
    val ba1 = new SimpleBigActor1("uav1",brs.nodes.get(2), scheduler)
    val ba0 = new SimpleBigActor0("uav0", brs.nodes.get(1), brs.rules.get(0), ba1, scheduler)
    scheduler.start()
    ba0.start()

  }
}
