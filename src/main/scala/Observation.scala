package bigactors
import edu.berkeley.eloi.bigraph._

class Observation (val obs : Array[Place]) extends Serializable {
  override
  def toString() = "[" + obs.deep.mkString(",") + "]"

  def contains(node: Place): Boolean = obs.contains(node)

}


