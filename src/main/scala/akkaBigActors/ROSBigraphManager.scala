package bigactors.akkaBigActors

import akka.actor.{Actor, ActorSystem, Props}
import akka.event.Logging
import akka.util.Timeout
import bigactors.{OBSERVATION_REQUEST_AKKA, BIGRAPH_REQUEST}
import edu.berkeley.eloi.bigraph.{Place, Bigraph, BRR}
import java.net.URI
import java.net.URISyntaxException

import org.apache.commons.logging.Log
import org.ros.address.InetAddressFactory
import org.ros.message.MessageListener
import org.ros.namespace.GraphName
import org.ros.node.AbstractNodeMain
import org.ros.node.ConnectedNode
import org.ros.node.DefaultNodeMainExecutor
import org.ros.node.NodeConfiguration
import org.ros.node.NodeMainExecutor
import org.ros.node.topic.Subscriber

import scala.concurrent.Await
import akka.event.Logging
import akka.pattern.ask
import scala.concurrent.duration._


/**
 * Created by eloi on 09-07-2014.
 */
class ROSBigraphManager(host: String = "localhost", port: Int = 11311) extends BigraphManager{
  implicit val system = ActorSystem("localBigraphManagerSystem")

  var bigraph = new Bigraph()

  override def executeBRR(brr: BRR): Unit = ???
  override def getBigraph: Bigraph = bigraph


  val node: AbstractNodeMain = new AbstractNodeMain {
    override def getDefaultNodeName: GraphName = GraphName.of("bigraphManager/listener")
    override def onStart(connectedNode: ConnectedNode): Unit ={
      val log: Log = connectedNode.getLog
      val subscriber: Subscriber[std_msgs.String] = connectedNode.newSubscriber("bigraph", std_msgs.String._TYPE)
      subscriber.addMessageListener(new MessageListener[std_msgs.String] {
        override def onNewMessage(msg: std_msgs.String): Unit = {
          log.info("[BigraphManager]: received new bgm term message")
          bigraph = new Bigraph(msg.getData + ";")
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
