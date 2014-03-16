package bigactors

import edu.berkeley.eloi.bigraph._
import collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

object SimpleQueryCompiler {
  def generate (input: String, host : Place, bigraph: Bigraph) :Array[Place] = {
    val splitedInput = input.split('.').reverse
    val it = splitedInput.iterator
    var result = new ArrayBuffer[Place]()
    result += host
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
            resultTmp.appendAll(Array(bigraph.getParentOf(it2.next())))
          }
        }
        case "linkedTo" => {
          while (it2.hasNext){
            resultTmp ++= bigraph.getLinkedTo(it2.next().asInstanceOf[BigraphNode])
          }
        }
        case _ => System.err.println("Syntax error while parsing query.")
      }
      result = resultTmp
    }
    result.toArray
  }
}
