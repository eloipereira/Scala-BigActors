import java.io.FileOutputStream
import java.nio.file.Paths
import java.util.Properties

import bigactors.BigActor._
import bigactors._
import edu.berkeley.eloi.bigraph.{Bigraph, Place}
import org.scalatest.FunSuite
import scala.collection.JavaConversions._
import scala.actors.Actor._

/**
 * Created by eloi on 01-07-2014.
 */

class TestBigActors extends FunSuite {
  // Configuration
  val prop = new Properties()
  prop.setProperty("RemoteBigActors","false")
  val p0 = Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/simple.bgm")
  prop.setProperty("bgmPath",p0.toString)
  prop.setProperty("visualization","false")
  prop.store(new FileOutputStream("config.properties"),null)

  test("Communication"){
    val a1 = BigActor hosted_at "u0" with_behavior{
      react{
        case msg: String => assert(msg equals "test")
      }
    }
    val a0 = BigActor hosted_at "u0" with_behavior{
      a1 ! "test"
    }
  }

  test("Asynchronous Observation"){
    BigActor hosted_at "u0" with_behavior{
      observe(Parent(Host))
      react{
        case obs: Array[Place] => assert(obs.head.getId.asInstanceOf[String] == "l0")
      }
    }
  }

  test("Synchronous observation"){
    BigActor hosted_at "u0" with_behavior{
      val obs: Bigraph = PARENT_HOST
      assert(obs.getNodes.head.getId == "l0")
    }
  }

  test("BRR MOVE_HOST_TO loc application"){
    BigActor hosted_at "u0" with_behavior{
      MOVE_HOST_TO("l1")
      assert(PARENT_HOST.getNodes.head.getId == "l1")
    }
  }

  test("Migration"){
    BigActor hosted_at "u0" with_behavior{
      migrate('u1)
      assert(HOST.getNodes.head.getId == "u1")
    }
  }
}
