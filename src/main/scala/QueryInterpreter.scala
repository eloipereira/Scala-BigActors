package bigactors

/**
 * Deep embedding DSL for the bigactor querying language
 */

import akka.actor.ActorRef
import edu.berkeley.eloi.bigraph._

import scala.actors.OutputChannel
import scala.collection.JavaConversions._
import scala.collection.mutable.{ArrayBuffer, HashMap}
import scala.reflect.ClassTag
import scala.util.parsing.combinator.JavaTokenParsers

sealed trait Query
trait QueryBigraph extends Query
trait QueryBigActors extends Query
trait Node extends QueryBigraph

case object Host extends Node
case class Parent(n: Node) extends Node
case class Children(n: Node) extends QueryBigraph
case class Linked_to(n: Node) extends QueryBigraph
case class Hosted_at(q: QueryBigraph) extends QueryBigActors

object QueryInterpreter {
//
//  def AkkaEvaluate (query: Query)(implicit hostId: String, bigraph: Bigraph, hostingRelation: HashMap[ActorRef,Symbol]):Either[Array[Place],Array[ActorRef]] = {
//    query match {
//      case query: QueryBigraph => Left(evaluateBigraph(query))
//      case query: QueryBigActors => Right(evaluateBigActors[ActorRef](query))
//    }
//  }

  def evaluate[A:ClassTag](query: Query, hostId: Symbol,  bigraph: Bigraph,  hostingRelation: HashMap[A,Symbol]):Either[Array[Place],Array[A]] = {
    query match {
      case query: QueryBigraph => Left(evaluateBigraph(query,hostId,bigraph))
      case query: QueryBigActors => Right(evaluateBigActors[A](query,hostId,bigraph,hostingRelation))
    }
  }

  def evaluateBigraph(query: QueryBigraph, hostId: Symbol, bigraph: Bigraph):Array[Place] = {
    query match {
      case Host => Array(bigraph.getNode(hostId.name))
      case Parent(q) => Array(bigraph.getParentOf(evaluateBigraph(q,hostId,bigraph).head.getId.asInstanceOf[String]))
      case Children(q) => Array[Place]() ++ bigraph.getChildrenOf(evaluateBigraph(q,hostId,bigraph).head)
      case Linked_to(q) => Array[Place]() ++ bigraph.getLinkedTo(evaluateBigraph(q,hostId,bigraph).head.getId.asInstanceOf[String])
    }
  }

  def evaluateBigActors[A:ClassTag](query: QueryBigActors, hostId: Symbol, bigraph: Bigraph, hostingRelation: HashMap[A,Symbol]): Array[A] = {
    query match {
      case Hosted_at(q) => {
        val bigraphNodes = evaluateBigraph(q,hostId,bigraph).map(p => p.getId.asInstanceOf[String])
        hostingRelation.filterKeys(a => bigraphNodes contains hostingRelation(a).name).keySet.toArray
      }
    }
  }


//  def evaluateRemote (query: Query)(implicit hostId: String, bigraph: Bigraph, hostingRelation: HashMap[Symbol,Symbol]):Either[Array[Place],ArrayBuffer[Symbol]] = {
//    query match {
//      case query: QueryBigraph => Left(evaluateBigraph(query))
//      case query: QueryBigActors => Right(evaluateRemoteBigActors(query))
//    }
//  }
//
//  def evaluateRemoteBigActors(query: QueryBigActors)(implicit hostId: String, bigraph: Bigraph, hostingRelation: HashMap[Symbol,Symbol]): ArrayBuffer[Symbol] = {
//    query match {
//      case Hosted_at(q) => {
//        val bigraphNodes = evaluateBigraph(q)
//        val bigactors = ArrayBuffer.empty[Symbol]
//        val reverseHostRelation = hostingRelation groupBy {_._2} map {case (key,value) => (key, value.unzip._1)}
//        bigraphNodes.foreach{b =>
//          val id = Symbol(b.getId.asInstanceOf[String])
//          if (reverseHostRelation.contains(id)){
//            reverseHostRelation(id).foreach{a =>
//              bigactors+=a
//            }
//          }
//        }
//        bigactors
//      }
//    }
//  }

  def evaluateString[A:ClassTag](q: String, hostId : Symbol, bigraph: Bigraph, hostingRelation: HashMap[A,Symbol] ) :Either[Array[Place],Array[A]] = {
    val queryInterpreter = new QueryParser()
    evaluate(queryInterpreter.parseAll(queryInterpreter.query,q).get,hostId,bigraph,hostingRelation)
  }

  class QueryParser(implicit composeSymbol: Char = '.') extends JavaTokenParsers{
    def host = "host"
    def parent= "parent"
    def children = "children"
    def linkedTo = "linkedTo"
    def hostedAt = "hostedAt"

    def node: Parser[Node] =
      host ^^
        {case _ => Host} |
      parent~composeSymbol~node ^^
        {case _~_~nodeVal => Parent(nodeVal)}

    def queryBg: Parser[QueryBigraph] =
      node |
      children~composeSymbol~node ^^
        {case _~_~nodeVal => Children(nodeVal)} |
        linkedTo~composeSymbol~node ^^
          {case _~_~nodeVal => Linked_to(nodeVal)}

    def queryBA: Parser[QueryBigActors] =
      hostedAt~composeSymbol~queryBg ^^
        {case _~_~bgVal => Hosted_at(bgVal)}

    def query: Parser[Query] = queryBA | queryBg
  }
}
