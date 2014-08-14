package bigactors.akkaBigActors

import java.net.{URI, URISyntaxException}
import java.nio.file.Paths

import edu.berkeley.eloi.bigraph.{BRR, BRS}
import org.apache.commons.logging.Log
import org.ros.RosCore
import org.ros.address.InetAddressFactory
import org.ros.concurrent.CancellableLoop
import org.ros.message.MessageListener
import org.ros.namespace.GraphName
import org.ros.node._
import org.ros.node.topic._

import scala.collection.JavaConversions._

/**
 * Created by eloi on 09-07-2014.
 */
object ROSBigraphSimulator extends App {

  val host = "localhost"
  val port = 11311
  //val initialBigraph: Bigraph = new Bigraph("l0_Location.r0_Robot[network] | l1_Location.r1_Robot[network] | l2_Location.r2_Robot[network] | l3_Location.r3_Robot[network] | l4_Location.r4_Robot[network];")
  RosCore.newPublic( host,port).start()

  val brs = new BRS(Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/robots.bgm").toString,true)
  //brs.update(initialBigraph.getTerm)
  val node: AbstractNodeMain = new AbstractNodeMain {
    override def getDefaultNodeName: GraphName = GraphName.of("bigraphSimulator")
    override def onStart(connectedNode: ConnectedNode): Unit ={
      val log: Log = connectedNode.getLog

      val subscriber: Subscriber[std_msgs.String] = connectedNode.newSubscriber("brr", std_msgs.String._TYPE)
      subscriber.addMessageListener(new MessageListener[std_msgs.String] {
        override def onNewMessage(msg: std_msgs.String): Unit = {
          log.info("[ROSBigraphSimulator]: received brr")
          brs.applyRules(List(new BRR(msg.getData)))
        }
      })

      val publisher: Publisher[std_msgs.String] = connectedNode.newPublisher("bigraph",std_msgs.String._TYPE)
      connectedNode.executeCancellableLoop(new CancellableLoop {
        override def loop(): Unit = {
          val  str :std_msgs.String = publisher.newMessage()
          str.setData(brs.getBigraph.getTerm)
          publisher.publish(str)
          log.info("Current Bigraph: "+ brs.getBigraph.getTerm)
          Thread.sleep(1000)
        }
      })

      super.onStart(connectedNode)
    }
  }


  val executor: NodeMainExecutor = DefaultNodeMainExecutor.newDefault
  executor.execute(node, setupConfiguration)
  def getMasterUri: URI  = {
    try new URI("http", null, host,port, "/", null, null)
    catch {
      case e : URISyntaxException => null
    }
  }
  def setupConfiguration: NodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback.getHostName,getMasterUri)


}
