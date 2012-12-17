package test

import bigactors._
import scala.actors._

object TestBigActors {

  class SimpleBigActor(host: Node, bigraphSchdl: Actor) extends BigActor(host, bigraphSchdl){
    def act(){
        observe(new Query)
        control(new BRR("MOVE"))
      }
  }

  def main(args: Array[String]){
    val scheduler = new BigraphSchdl
    val ba0 = new SimpleBigActor(new Node, scheduler)
    scheduler.start()
    ba0.start()
    ba0.host = new Node
  }
}
