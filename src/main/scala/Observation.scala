package bigactors
import edu.berkeley.eloi.bigraph._

class Observation (val bigraph : Array[Place]) extends Serializable {
  override
  def toString() = "[" + bigraph.deep.mkString(",") + "]"

  def contains(node: Place): Boolean = bigraph.contains(node)



}


