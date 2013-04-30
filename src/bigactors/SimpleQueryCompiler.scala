package bigactors

import edu.berkeley.eloi.bigraph._
import collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

object SimpleQueryCompiler {
  def generate (input: String, host : Node, bigraph: Bigraph) :Array[Node] = {
    val splitedInput = input.split('.').reverse
    val it = splitedInput.iterator
    var result = new ArrayBuffer[Node]()
    result += host
    if (!it.next().equals("host")){
      System.err.println("Queries must always refer to host.")
    }
    while (it.hasNext){
      val it2 = result.iterator
      val resultTmp = new ArrayBuffer[Node]()
      it.next() match {
        case "children" => {
          while (it2.hasNext){
            resultTmp ++= bigraph.childrenOf(it2.next().getId)
          }
        }
        case "parent" => {
          while (it2.hasNext){
            resultTmp.appendAll(Array(bigraph.parentOf(it2.next().getId)))
          }
        }
        case "linkedTo" => {
          while (it2.hasNext){
            resultTmp ++= bigraph.linkedTo(it2.next().getId)
          }
        }
        case _ => System.err.println("Syntax error while parsing query.")
      }
      result = resultTmp
    }
    result.toArray
  }
}
