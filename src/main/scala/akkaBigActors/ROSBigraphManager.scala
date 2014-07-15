package bigactors.akkaBigActors

import akka.actor.{ActorRef, Actor, ActorSystem, Props, Stash}
import akka.event.Logging
import akka.util.Timeout
import bigactors.{EXECUTE_BRR, BIGRAPH_RESPONSE, OBSERVATION_REQUEST_AKKA, BIGRAPH_REQUEST}
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


case class BIGRAPH_REQUEST_WITH_REF(ref: ActorRef)

/**
 * Created by eloi on 09-07-2014.
 */
class ROSBigraphManager(host: String = "localhost", port: Int = 11311) extends BigraphManager {

  import context._

  var configured = false

  val handler = context.actorOf(Props(classOf[ROSBigraphHandler], "localhost", 11311))


  override def executeBRR(brr: BRR): Unit = handler

  override def getBigraph: Bigraph = {
    implicit val timeout = Timeout(5 seconds)
    val future = handler ? BIGRAPH_REQUEST
    Await.result(future, timeout.duration) match {
      case BIGRAPH_RESPONSE(bg) => bg
    }
  }

}

class ROSBigraphHandler(host: String = "localhost", port: Int = 11311) extends Actor with Stash{
  var bigraph = new Bigraph()
  import context._

  var configured = false

  def ready:Receive = {
    case BIGRAPH_REQUEST =>
      sender ! BIGRAPH_RESPONSE(bigraph)
    case EXECUTE_BRR(brr) =>
  }


  def receive: Receive = {
    case "configured" =>
      unstashAll()
      configured = true
      become(ready, discardOld = false)
    case _ => stash()
  }


    val node: AbstractNodeMain = new AbstractNodeMain {
      override def getDefaultNodeName: GraphName = GraphName.of("bigraphManager/listener")
      override def onStart(connectedNode: ConnectedNode): Unit = {
        val log: Log = connectedNode.getLog
        val subscriber: Subscriber[std_msgs.String] = connectedNode.newSubscriber("bigraph", std_msgs.String._TYPE)
        subscriber.addMessageListener(new MessageListener[std_msgs.String] {
          override def onNewMessage(msg: std_msgs.String): Unit = {
            log.info("[BigraphManager]: received new bgm term message")
            bigraph = new Bigraph(msg.getData + ";")
            if (!configured){
              self ! "configured"
            }

          }
        })
        super.onStart(connectedNode)
      }
    }

    val executor: NodeMainExecutor = DefaultNodeMainExecutor.newDefault
    executor.execute(node, setupConfiguration)
    def getMasterUri: URI = {
      try new URI("http", null, host, port, "/", null, null)
      catch {
        case e: URISyntaxException => null
      }
    }
    def setupConfiguration: NodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback.getHostName, getMasterUri)

}
