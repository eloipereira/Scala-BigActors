package bigactors.akkaBigActors

import edu.berkeley.eloi.bigraph.{BRR, BRS, Bigraph}

import scala.collection.JavaConversions._



class BigMCBigraphManager( bgmPath: String, visualization: Boolean = false, log: Boolean = false) extends BigraphManager {
  var brs: BRS = new BRS(bgmPath,log,visualization)
  def executeBRR(brr :BRR): Unit = brs.applyRules(List(brr),2)  
  def getBigraph: Bigraph = brs.getBigraph
}
