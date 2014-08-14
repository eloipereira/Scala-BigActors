package bigactors.akkaBigActors

import edu.berkeley.eloi.bigraph.{BRR, BRS, Bigraph}

import scala.collection.JavaConversions._



class BigMCBigraphManager( bgmPath: String, visualization: Boolean = false) extends BigraphManager {
  var brs: BRS = new BRS(bgmPath,visualization)
  def executeBRR(brr :BRR): Unit = brs.applyRules(List(brr))
  def getBigraph: Bigraph = brs.getBigraph
}
