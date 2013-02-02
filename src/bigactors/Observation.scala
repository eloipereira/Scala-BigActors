package bigactors
import edu.berkeley.eloi.bigraph._

class Observation (val obs : Array[Node]) {
  override
  def toString() = "[" + obs.deep.mkString(",") + "]"
}


