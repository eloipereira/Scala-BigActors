/**
 * Created by eloi on 08-07-2014.
 */

import java.net.{URI, URISyntaxException}

import org.ros.RosCore
import org.ros.address.InetAddressFactory
import org.ros.namespace.GraphName
import org.ros.node.{AbstractNodeMain, ConnectedNode, DefaultNodeMainExecutor, NodeConfiguration, NodeMainExecutor}

object ExampleROS extends App{

  RosCore.newPublic( "localhost",11311).start()
  val executor: NodeMainExecutor = DefaultNodeMainExecutor.newDefault
  val node: AbstractNodeMain = new AbstractNodeMain {
    override def getDefaultNodeName: GraphName = GraphName.of("myNode")
    override def onStart(connectedNode: ConnectedNode): Unit ={
      connectedNode.getLog.info("Test!!!!!")
      super.onStart(connectedNode)
    }
  }

  executor.execute(node, setupConfiguration)


  def getMasterUri: URI  = {
    try new URI("http", null, "localhost",11311, "/", null, null)
    catch {
      case e : URISyntaxException => null
    }
  }

  def setupConfiguration: NodeConfiguration = NodeConfiguration.newPublic(InetAddressFactory.newNonLoopback.getHostName,getMasterUri)



}
