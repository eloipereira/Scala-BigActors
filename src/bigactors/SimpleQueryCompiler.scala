package bigactors

import edu.berkeley.eloi.bigraph._
import collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._

object SimpleQueryCompiler {
  def generate (input: String, bigActor : BigActor, bigraph: Bigraph) :Array[Node] = {
    val splitedInput = input.split(".").reverse
    val host : Node = bigActor.getHost
    val it = splitedInput.iterator
    val result = new ArrayBuffer[Node]()
    result += host
    if (!it.next().equals("host")){
      System.err.println("Queries must alawys refer to host.")
    }
    while (it.hasNext){
      val it2 = result.iterator
      result.clear()
      it.next() match {
        case "children" => {
          while (it.hasNext){
            result ++= bigraph.childrenOf(it2.next())
          }
        }
        case "parent" => {
          while (it.hasNext){
            result ++= Array(bigraph.parentOf(it2.next()))
          }
        }
        case "linkedTo" => {
          while (it.hasNext){
            result ++= bigraph.linkedTo(it2.next())
          }
        }
        case _ => System.err.println("Syntax error while parsing query.")
      }
    }
    result.toArray
  }
}
