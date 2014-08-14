import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.BigActor._
import bigactors._
import edu.berkeley.eloi.bigraph.Bigraph
import org.scalatest.FunSuite

import scala.actors.Actor._
import scala.actors.OutputChannel
import scala.collection.mutable
import scala.collection.mutable._

/**
 * Created by eloi on 01-07-2014.
 */

class TestQueries extends FunSuite {
  val bigraph = new Bigraph("l0_Location.(u0_UAV[network] | u1_UAV[network]) | l1_Location;")
  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/simple.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","false")
  prop.store(new FileOutputStream("config.properties"),null)

  test("Host"){
    assert(QueryInterpreter.evaluateBigraph(Host,'u0,bigraph).head.getId.asInstanceOf[String] == "u0")
    assert(QueryInterpreter.evaluateString("host",'u0,bigraph, new mutable.HashMap[OutputChannel[Any],Symbol]()).left.get.head.getId.asInstanceOf[String] == "u0")
  }
  test("Parent of host"){
    assert(QueryInterpreter.evaluateBigraph(Parent(Host),'u0,bigraph).head.getId.asInstanceOf[String] == "l0")
    assert(QueryInterpreter.evaluateBigraph(Parent(Host),'u0,bigraph).head.getId.asInstanceOf[String] == "l0")
  }
  test("Linked to host"){
    assert(QueryInterpreter.evaluateBigraph(Linked_to(Host),'u0,bigraph).deep == QueryInterpreter.evaluateString("linkedTo.host",'u0,bigraph, new mutable.HashMap[OutputChannel[Any],Symbol]()).left.get.deep)
  }
  test("Children of parent of host"){
    assert(QueryInterpreter.evaluateBigraph(Children(Parent(Host)),'u0,bigraph).deep == QueryInterpreter.evaluateString("children.parent.host",'u0,bigraph, new mutable.HashMap[OutputChannel[Any],Symbol]()).left.get.deep)
  }
  test("Children of children of parent of host"){
    assert(QueryInterpreter.evaluateBigraph(Children(Parent(Parent(Host))),'u0,bigraph).deep == QueryInterpreter.evaluateString("children.parent.parent.host",'u0,bigraph, new mutable.HashMap[OutputChannel[Any],Symbol]()).left.get.deep)
  }
  test("Hosted at host"){
    val a = BigActor hosted_at "u0" with_behavior{
      receive{
        case msg: String => assert(msg == "DONE")
      }
    }
    actor{
      BigActorSchdl ! REQUEST_HOSTING_RELATION
      react{
        case h: HashMap[OutputChannel[Any],Symbol] => {
          QueryInterpreter.evaluateBigActors(Hosted_at(Host),'u0,bigraph,h).head ! "DONE"
        }
      }
    }

  }
}
