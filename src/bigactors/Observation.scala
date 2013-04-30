package bigactors
import edu.berkeley.eloi.bigraph._

class Observation (val obs : Array[Node]) {
  override
  def toString() = "[" + obs.deep.mkString(",") + "]"

  def contains(node: Node): Boolean = obs.contains(node)
}


