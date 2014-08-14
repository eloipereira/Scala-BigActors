package bigactors
package remote


import java.io.FileInputStream
import java.net.InetAddress
import java.util.Properties

import edu.berkeley.eloi.bigraph._
import org.apache.commons.logging.{Log, LogFactory}

import scala.actors.Actor._
import scala.actors.remote.Node
import scala.actors.remote.RemoteActor._
import scala.actors.{Actor, OutputChannel}
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap


object RemoteBigActorSchdl extends Actor with App {
  var debug = true

  private val log: Log = LogFactory.getLog("RemoteBigActorSchdl")


  // scala.actors.Debug.level_=(100)

  private implicit val hostingRelation = new HashMap[Symbol,Symbol]
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
    log.debug("BigActorSchdl operating remotely at IP "+ schdlIP + " and port "+ schdPort.toInt)

    val managerID = Symbol(prop.getProperty("BigraphManagerID"))
    val managerPort = prop.getProperty("BigraphManagerPort").toInt
    val managerIP = prop.getProperty("BigraphManagerIP")
    val bigraphManager = select(Node(managerIP,managerPort), managerID)


    loop {
      react{
        case REMOTE_HOSTING_REQUEST(bigActorID,ip,port,hostID) =>{
          log.debug("Got a host request from " + sender + " to be hosted at "+hostID)
          val bigActor = sender

          bigraphManager ! BIGRAPH_REQUEST
          react {
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.contains(new BigraphNode(hostID.name))) {
                log.debug("Hosting BigActor at host " + hostID)
                hostingRelation += bigActorID -> hostID
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
          log.debug("Got a obs request with query " + query + " from "+bigActorID)
          bigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val hostId: Symbol = hostingRelation(bigActorID)
              val result = QueryInterpreter.evaluate(query,hostId,bigraph,hostingRelation)
              result match {
                case Left(b) => {
                  log.debug("Observed Bigraph: " + b)
                  proxies(bigActorID) ! b
                }
                case Right(a) => {
                  log.debug("Observed BigActors: " + a)
                  proxies(bigActorID) ! a
                }
              }
            }
          }
        }
        case REMOTE_CONTROL_REQUEST(brr, bigActorID) => {
          log.debug("Got a ctr request " + brr)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (brr.getRedex.getNodes.contains(bigraph.getNode(hostingRelation(bigActorID).name))
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostingRelation(bigActorID).name))){
                bigraphManager ! EXECUTE_BRR(brr)
              } else {
                System.err.println("Host " + hostingRelation(bigActorID) + "is not included on redex/reactum of "+ brr)
                System.exit(0)
              }
            }
          }
        }
        case REMOTE_SEND_REQUEST(msg, rcvID, bigActorID) => {
          val senderID = bigActorID
          log.debug("Got a snd request from " + bigActorID)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostingRelation(senderID).name)
              val destHost = bigraph.getNode(hostingRelation(rcvID).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                log.debug("Hosts " + hostingRelation(senderID).name + " and " +  hostingRelation(rcvID).name + " are connected.")
                val rcv = proxies(rcvID)
                rcv ! msg
              } else {
                log.warn("Hosts " + hostingRelation(senderID).name + " and " +  hostingRelation(rcvID).name + " are not connected.")
              }
            }
          }
        }
        case REMOTE_MIGRATION_REQUEST(newHostId, bigActorID) => {
          log.debug("Got a mgrt request from " + hostingRelation(bigActorID) + " to " +newHostId)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              val currentHost = bigraph.getNode(hostingRelation(bigActorID).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                log.debug("Hosts connected. Migrating...")
                hostingRelation += bigActorID -> newHostId
              } else {
                log.warn("Hosts " + hostingRelation(bigActorID) + " and " + newHostId + " are not connected.")
              }
            }
          }
        }
        case _ => log.warn("Unknown request.")
      }
    }
  }
  start

}
