package test

import bigactors.SimpleQueryCompiler
import edu.berkeley.eloi.concreteBgm2Java.ConcreteBgm2JavaCompiler
import edu.berkeley.eloi.bigraph.BRS

object TestQuery {
  def main (args : Array[String]){
    val gen = ConcreteBgm2JavaCompiler.generate("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true)
    val brs: BRS = new BRS(gen.signature, gen.names, gen.bigraph, gen.rules)
    val result = SimpleQueryCompiler.generate("children.parent.host",brs.getBigraph.getNode("u0"),brs.getBigraph)
    for (r <- result){
      println(r)
    }
  }
}
