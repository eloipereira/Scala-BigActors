package bigactors.examples

import bigactors.QueryInterpreter
import edu.berkeley.eloi.bigraph.{BRR, BRS}
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler

import scala.actors.OutputChannel
import scala.collection.JavaConversions._
import scala.collection.mutable

object ExamplesStringQueries {
  def main (args : Array[String]){
    val gen = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/REP13.bgm",true)
    val brs: BRS = new BRS(gen.signature, gen.names, gen.bigraph, gen.rules,false)
    val result = QueryInterpreter.evaluateString("children.parent.host",'u0,brs.getBigraph, new mutable.HashMap[OutputChannel[Any],Symbol]()).left
    for (r <- result){
      println(r)
    }
    println(brs.getBigraph.getNode("tanker0").getParent)
    println("Apply rule")
    val r: BRR = brs.getRules.get(0)
    brs.applyRules(List(r))
    println(brs)
    val result1 = QueryInterpreter.evaluateString("children.parent.host",'u0,brs.getBigraph, new mutable.HashMap[OutputChannel[Any],Symbol]()).left
    for (r <- result1){
      println(r)
    }
    println(brs.getBigraph.getNode("tanker0").getParent)
    println("Apply rule")
    val r1: BRR = brs.getRules.get(1)
    brs.applyRules(List(r1))
    println(brs)
    val result2 = QueryInterpreter.evaluateString("children.parent.host",'u0,brs.getBigraph, new mutable.HashMap[OutputChannel[Any],Symbol]()).left
    for (r <- result2){
      println(r)
    }
  }
}
