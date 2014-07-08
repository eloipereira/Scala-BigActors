/**
 * Created by eloi on 08-07-2014.
 */

import java.net.URI
import java.net.URISyntaxException

import org.ros.address.InetAddressFactory
import org.ros.namespace.GraphName
import org.ros.node.AbstractNodeMain
import org.ros.node.ConnectedNode
import org.ros.node.DefaultNodeMainExecutor
import org.ros.node.NodeConfiguration
import org.ros.node.NodeMainExecutor
import org.ros.RosCore

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
