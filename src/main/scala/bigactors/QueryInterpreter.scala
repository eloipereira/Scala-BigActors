package bigactors

/**
 * Deep embedding DSL for the bigactor querying language
 */

import java.util

import edu.berkeley.eloi.bigraph._
import org.apache.commons.logging.{LogFactory, Log}

import scala.collection.JavaConversions._
import scala.collection.mutable

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
case object All extends QueryBigraph
case class Hosted_at(q: QueryBigraph) extends QueryBigActors

object QueryInterpreter {

  private val log: Log = LogFactory.getLog("QueryInterpreter")

  def evaluate[A:ClassTag](query: Query, hostId: Symbol,  bigraph: Bigraph,  hostingRelation: HashMap[A,Symbol]):Either[Bigraph,Array[A]] = {
    query match {
      case All => Left(bigraph)
      case query: QueryBigraph =>  {
        val region = new Region(0)
        val places = evaluateBigraph(query,hostId,bigraph).getPlaces.map(p => {
          if(p.isInstanceOf[BigraphNode] & ! bigraph.getPlaces.contains(p.getParent)){
            p.setParent(region)
            p
          }
          else p
        })
        Left(new Bigraph(new PlaceList(places)))
      }
      case query: QueryBigActors => Right(evaluateBigActors[A](query,hostId,bigraph,hostingRelation))
    }
  }

  def evaluateBigraph(query: QueryBigraph, hostId: Symbol, bigraph: Bigraph):Bigraph = {
    query match {
      case Host => {
        val hostNode = bigraph.getNode(hostId.name)
        val hostParent = hostNode.getParent
        val region = new Region(0)
        new Bigraph(new PlaceList(List(hostNode,hostParent,region)))
      }
      case Parent(q) => {
        val node = evaluateBigraph(q,hostId,bigraph).getPlaces.head
        if (node.isInstanceOf[Region]){
          log.error("Trying to evaluate the parent of a region, returning the region itself")
          new Bigraph(new PlaceList(List(node)))
        }
        else {
          val parentNode = bigraph.getParentOf(node.asInstanceOf[BigraphNode].getId)
          if(parentNode.isInstanceOf[Region]){
            val region = new Region(0)
            new Bigraph(new PlaceList(List(parentNode,region).toSet))
          } else {
            val parentOfParent = parentNode.getParent
            val region = new Region(0)
            new Bigraph(new PlaceList(List(parentNode,parentOfParent,region).toSet))
          }
        }
      }
      case Children(q) => {
        val node = evaluateBigraph(q,hostId,bigraph).getPlaces.head
        if(node.isInstanceOf[Region]){
          val children =  bigraph.getChildrenOf(node)
          new Bigraph(new PlaceList((children ++ List(node)).toSet))
        } else {
          val children =  bigraph.getChildrenOf(node)
          val region = new Region(0)
          new Bigraph(new PlaceList((children ++ List(node,region)).toSet))
        }
      }
      case Linked_to(q) => {
        val node = evaluateBigraph(q,hostId,bigraph).getNodes.head
        val region = new Region(0)
        val linked= bigraph.getLinkedTo(node.getId)
        val parentsOfLinked = linked.map(l => l.getParent)
        new Bigraph(new PlaceList((List[Place](region) ++ linked ++ parentsOfLinked).toSet))
      }
    }
  }

  def evaluateBigActors[A:ClassTag](query: QueryBigActors, hostId: Symbol, bigraph: Bigraph, hostingRelation: HashMap[A,Symbol]): Array[A] = {
    query match {
      case Hosted_at(q) => {
        val bigraphNodes = evaluateBigraph(q,hostId,bigraph).getNodes.map(p => p.getId)
        hostingRelation.filterKeys(a => bigraphNodes contains hostingRelation(a).name).keySet.toArray
      }
    }
  }

  def evaluateString[A:ClassTag](q: String, hostId : Symbol, bigraph: Bigraph, hostingRelation: HashMap[A,Symbol] ) :Either[Bigraph,Array[A]] = {
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
