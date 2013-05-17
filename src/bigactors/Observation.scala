package bigactors
import edu.berkeley.eloi.bigraph._

class Observation (val obs : Array[Place]) {
  override
  def toString() = "[" + obs.deep.mkString(",") + "]"

  def contains(node: Place): Boolean = obs.contains(node)
}


