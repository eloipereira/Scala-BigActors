package scalaBigraph

import edu.berkeley.eloi.bigraph._

import scala.collection.JavaConversions._


/**
 * Created by eloi on 7/28/14.
 */

trait ScalaBigraphNodeTrait{
  def toBgm: String
  def ~>(children: ScalaBigraph): ScalaBigraph = new ScalaBigraph(this.toBgm + ".(" + children.getTerm + ");")
}

trait ScalaBigraphTrait{
  def getTerm: String
  def |(bigraph: ScalaBigraph): ScalaBigraph = new ScalaBigraph(this.getTerm + "|" + bigraph.getTerm+ ";")
  def ||(bigraph: ScalaBigraph): ScalaBigraph = new ScalaBigraph(this.getTerm + "||" + bigraph.getTerm+ ";")
  def ==>(bigraph: ScalaBigraph): ScalaBRR = new ScalaBRR(new ScalaBigraph(this.getTerm),bigraph)
}

class ScalaBigraphHole(i: Integer) extends Hole(i,new Region(0))

class Node(name: String, links: List[String]) extends BigraphNode(name, "Node",links, new Region(0)) with ScalaBigraphNodeTrait{
  override
  def getCtrId = this.getClass.getSimpleName
  }

class ScalaBigraph(term: String) extends Bigraph(term + ";") with ScalaBigraphTrait{
  def matches(redex: ScalaBigraph): Boolean = {

    val brs: BRS = new BRS(new Bigraph(term),false,false)

    //TODO - introduce a method in BRS at Bgm2Bigraph to check the match

    ???
  }
   //TODO - implement equals and hashCode


}

class ScalaBRR(redex: ScalaBigraph, reactum: ScalaBigraph) extends BRR("",redex,reactum)

object ScalaBigraphImplicits {
  implicit def scalaBigraphNode2ScalaBigraph(node: Node):ScalaBigraph = new ScalaBigraph(node.toBgm)
  implicit def scalaBigraphHole2ScalaBigraph(hole: ScalaBigraphHole):ScalaBigraph = new ScalaBigraph(hole.toBgm)
  def $(i: Integer): ScalaBigraphHole = new ScalaBigraphHole(i)
}