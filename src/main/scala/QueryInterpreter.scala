package bigactors

import _root_.akka.actor.ActorRef
import edu.berkeley.eloi.bigraph._
import scala.actors.OutputChannel
import scala.collection.mutable.{HashMap, ArrayBuffer}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

trait Query
trait QueryBigraph extends Query
trait QueryBigActors extends Query
trait Node extends QueryBigraph

case object Host extends Node
case class Parent(n: Node) extends Node
case class Children(n: Node) extends QueryBigraph
case class Linked_to(n: Node) extends QueryBigraph
case class Hosted_at(q: QueryBigraph) extends QueryBigActors

object QueryInterpreter {

  def AkkaEvaluate (query: Query, hostId: String, bigraph: Bigraph, hostingRelation: HashMap[ActorRef,Symbol]):Either[Array[Place],ArrayBuffer[ActorRef]] = {
    query match {
      case query: QueryBigraph => Left(evaluateBigraph(query,hostId,bigraph))
      case query: QueryBigActors => Right(AkkaEvaluateBigActors(query,hostId,bigraph,hostingRelation))
    }
  }

  def evaluate (query: Query, hostId: String, bigraph: Bigraph, hostingRelation: HashMap[OutputChannel[Any],Symbol]):Either[Array[Place],ArrayBuffer[OutputChannel[Any]]] = {
    query match {
      case query: QueryBigraph => Left(evaluateBigraph(query,hostId,bigraph))
      case query: QueryBigActors => Right(evaluateBigActors(query,hostId,bigraph,hostingRelation))
    }
  }

  def evaluateBigraph(query: QueryBigraph, hostId: String, bigraph: Bigraph):Array[Place] = {
    query match {
      case Host => Array(bigraph.getNode(hostId))
      case Parent(q) => Array(bigraph.getParentOf(evaluateBigraph(q,hostId,bigraph).head.getId.asInstanceOf[String]))
      case Children(q) => Array[Place]() ++ bigraph.getChildrenOf(evaluateBigraph(q,hostId,bigraph).head)
      case Linked_to(q) => Array[Place]() ++ bigraph.getLinkedTo(evaluateBigraph(q,hostId,bigraph).head.getId.asInstanceOf[String])
    }
  }

  def evaluateBigActors(query: QueryBigActors, hostId: String, bigraph: Bigraph, hostingRelation: HashMap[OutputChannel[Any],Symbol]): ArrayBuffer[OutputChannel[Any]] = {
    query match {
      case Hosted_at(q) => {
        val bigraphNodes = evaluateBigraph(q,hostId,bigraph)
        val bigactors = ArrayBuffer.empty[OutputChannel[Any]]
        val reverseHostRelation = hostingRelation groupBy {_._2} map {case (key,value) => (key, value.unzip._1)}
        bigraphNodes.foreach{b =>
          val id = Symbol(b.getId.asInstanceOf[String])
          if (reverseHostRelation.contains(id)){
            reverseHostRelation(id).foreach{a =>
              bigactors+=a
            }
          }
        }
        bigactors
      }
    }
  }

  def AkkaEvaluateBigActors(query: QueryBigActors, hostId: String, bigraph: Bigraph, hostingRelation: HashMap[ActorRef,Symbol]): ArrayBuffer[ActorRef] = {
    query match {
      case Hosted_at(q) => {
        val bigraphNodes = evaluateBigraph(q,hostId,bigraph)
        val bigactors = ArrayBuffer.empty[ActorRef]
        val reverseHostRelation = hostingRelation groupBy {_._2} map {case (key,value) => (key, value.unzip._1)}
        bigraphNodes.foreach{b =>
          val id = Symbol(b.getId.asInstanceOf[String])
          if (reverseHostRelation.contains(id)){
            reverseHostRelation(id).foreach{a =>
              bigactors+=a
            }
          }
        }
        bigactors
      }
    }
  }

  def evaluateRemote (query: Query, hostId: String, bigraph: Bigraph, hostingRelation: HashMap[Symbol,Symbol]):Either[Array[Place],ArrayBuffer[Symbol]] = {
    query match {
      case query: QueryBigraph => Left(evaluateBigraph(query,hostId,bigraph))
      case query: QueryBigActors => Right(evaluateRemoteBigActors(query,hostId,bigraph,hostingRelation))
    }
  }

  def evaluateRemoteBigActors(query: QueryBigActors, hostId: String, bigraph: Bigraph, hostingRelation: HashMap[Symbol,Symbol]): ArrayBuffer[Symbol] = {
    query match {
      case Hosted_at(q) => {
        val bigraphNodes = evaluateBigraph(q,hostId,bigraph)
        val bigactors = ArrayBuffer.empty[Symbol]
        val reverseHostRelation = hostingRelation groupBy {_._2} map {case (key,value) => (key, value.unzip._1)}
        bigraphNodes.foreach{b =>
          val id = Symbol(b.getId.asInstanceOf[String])
          if (reverseHostRelation.contains(id)){
            reverseHostRelation(id).foreach{a =>
              bigactors+=a
            }
          }
        }
        bigactors
      }
    }
  }

  def evaluateString (query: String, hostId : String, bigraph: Bigraph) :Array[Place] = {
    val splitedInput = query.split('.').reverse
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
