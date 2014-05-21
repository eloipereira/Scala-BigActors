import bigactors.BigraphQueryCompiler
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler
import edu.berkeley.eloi.bigraph.{BRR, BRS}
import scala.collection.JavaConversions._

object TestQuery {
  def main (args : Array[String]){
    val gen = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/REP13.bgm",true)
    val brs: BRS = new BRS(gen.signature, gen.names, gen.bigraph, gen.rules,false)
    val result = BigraphQueryCompiler.interpret("children.parent.host","u0",brs.getBigraph)
    for (r <- result){
      println(r)
    }
    println(brs.getBigraph.getNode("tanker0").getParent)
    println("Apply rule")
    val r: BRR = brs.getRules.get(0)
    brs.applyRules(List(r),2)
    println(brs)
    val result1 = BigraphQueryCompiler.interpret("children.parent.host","u0",brs.getBigraph)
    for (r <- result1){
      println(r)
    }
    println(brs.getBigraph.getNode("tanker0").getParent)
    println("Apply rule")
    val r1: BRR = brs.getRules.get(1)
    brs.applyRules(List(r1),2)
    println(brs)
    val result2 = BigraphQueryCompiler.interpret("children.parent.host","u0",brs.getBigraph)
    for (r <- result2){
      println(r)
    }
  }
}
