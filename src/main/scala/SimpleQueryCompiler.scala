package bigactors

import edu.berkeley.eloi.bigraph._
import collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

object SimpleQueryCompiler {
  def generate (input: String, hostId : String, bigraph: Bigraph) :Array[Place] = {
    val splitedInput = input.split('.').reverse
    val it = splitedInput.iterator
    var result = new ArrayBuffer[Place]()
    result += bigraph.getNode(hostId)
    if (!it.next().equals("host")){
      System.err.println("Queries must always refer to host.")
    }
    while (it.hasNext){
      val it2 = result.iterator
      val resultTmp = new ArrayBuffer[Place]()
      it.next() match {
        case "children" => {
          while (it2.hasNext){
            resultTmp ++= bigraph.getChildrenOf(it2.next())
          }
        }
        case "parent" => {
          while (it2.hasNext){
            resultTmp.appendAll(Array(bigraph.getParentOf(it2.next().getId.asInstanceOf[String])))
          }
        }
        case "linkedTo" => {
          while (it2.hasNext){
            resultTmp ++= bigraph.getLinkedTo(it2.next().getId.asInstanceOf[String])
          }
        }
        case _ => System.err.println("Syntax error while parsing query.")
      }
      result = resultTmp
    }
    result.toArray
  }
}
