package bigactors


import scala.actors.remote.RemoteActor._
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug
import scala.collection.mutable.HashMap
import scala.actors._
import scala.actors.Actor._
import scala.actors.remote.Node
import java.util.Properties
import java.io.FileInputStream
import scala.collection.mutable
import java.net.InetAddress


object RemoteBigActorSchdl extends Actor with App {
  var debug = true

  scala.actors.Debug.level_=(100)

  private val hostRelation = new HashMap[Symbol,Symbol]
 // private val remoteNodeMap = new HashMap[Symbol,Node]
  private val proxies = new HashMap[Symbol,OutputChannel[Any]]



  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
  if (!remote){
    System.err.println("Configuration file not set for remote bigActors",debug)
    System.exit(0)
  }


  def act() {

    val schdPort = prop.getProperty("BigActorSchdlPort").toInt
    val localhost = InetAddress.getLocalHost
    val schdlIP = localhost.getHostAddress
    val schdID = Symbol(prop.getProperty("BigActorSchdlID"))

    alive(schdPort)
    register(schdID, self)
    Debug.println("BigActorSchdl operating remotely at IP "+ schdlIP + " and port "+ schdPort.toInt,debug)

    val managerID = Symbol(prop.getProperty("BigraphManagerID"))
    val managerPort = prop.getProperty("BigraphManagerPort").toInt
    val managerIP = prop.getProperty("BigraphManagerIP")
    val bigraphManager = select(Node(managerIP,managerPort), managerID)


    loop {
      react{
        case REMOTE_HOSTING_REQUEST(bigActorID,ip,port,hostID) =>{
          Debug.println("got a host request from " + sender + " to be hosted at "+hostID,debug)
          val bigActor = sender
          bigraphManager ! BIGRAPH_REQUEST
          react {
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.contains(new BigraphNode(hostID.name))) {
                Debug.println("Hosting BigActor at host " + hostID,debug)
                hostRelation += bigActorID -> hostID
                val node = Node(ip,port)
              //  remoteNodeMap += bigActorID -> node
                proxies += bigActorID -> bigActor
                bigActor ! REMOTE_HOSTING_SUCCESSFUL
              }
              else {
                System.err.println("BigActor cannot be hosted at " + hostID + ". Make sure host exists!")
                System.exit(0)
              }
            }
          }
        }
        case REMOTE_OBSERVATION_REQUEST(query, bigActorID) => {
          Debug.println("got a obs request with query " + query + " from "+bigActorID,debug)
          bigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val host = bigraph.getNode(hostRelation(bigActorID).name)
              val obs = new Observation(SimpleQueryCompiler.generate(query,host,bigraph))
              Debug.println("Observation: "+obs,debug)
              proxies(bigActorID) ! obs

            }
          }
        }
        case REMOTE_CONTROL_REQUEST(brr, bigActorID) => {
          Debug.println("got a ctr request " + brr,debug)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (brr.getRedex.getNodes.contains(bigraph.getNode(hostRelation(bigActorID).name))
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostRelation(bigActorID).name))){
                bigraphManager ! EXECUTE_BRR(brr)
              } else {
                System.err.println("Host " + hostRelation(bigActorID) + "is not included on redex/reactum of "+ brr)
                System.exit(0)
              }
            }
          }
        }
        case REMOTE_SEND_REQUEST(msg, bigActorID) => {
          val senderID = bigActorID
          val receiverID = msg.receiverID
          Debug.println("got a snd request from " + bigActorID,debug)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostRelation(senderID).name)
              val destHost = bigraph.getNode(hostRelation(receiverID).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are connected.",debug)
                val rcv = proxies(receiverID)
                rcv ! msg.message
              } else {
                System.err.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case REMOTE_MIGRATION_REQUEST(newHostId, bigActorID) => {
          Debug.println("got a mgrt request from " + hostRelation(bigActorID) + " to " +newHostId,debug)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              val currentHost = bigraph.getNode(hostRelation(bigActorID).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts connected. Migrating...",debug)
                hostRelation += bigActorID -> newHostId
              } else {
                System.err.println("Hosts " + hostRelation(bigActorID) + " and " + newHostId + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case _ => println("UNKNOWN REQUEST")
      }
    }
  }
  start

}
