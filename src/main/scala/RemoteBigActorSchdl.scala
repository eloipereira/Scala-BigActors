package bigactors
package remote


import java.io.FileInputStream
import java.net.InetAddress
import java.util.Properties

import edu.berkeley.eloi.bigraph._
import edu.berkeley.eloi.concreteBgm2Java.Debug

import scala.actors.{OutputChannel, Actor}
import scala.actors.Actor._

import scala.actors.remote.Node
import scala.actors.remote.RemoteActor._
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap


object RemoteBigActorSchdl extends Actor with App {
  var debug = true

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
          Debug.println("got a obs request with query " + query + " from "+bigActorID,debug)
          bigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val hostId: Symbol = hostingRelation(bigActorID)
              val result = QueryInterpreter.evaluate(query,hostId,bigraph,hostingRelation)
              result match {
                case Left(b) => {
                  Debug.println("[BigActorSchdl]:\t Observed Bigraph: " + b, debug)
                  proxies(bigActorID) ! b
                }
                case Right(a) => {
                  Debug.println("[BigActorSchdl]:\t Observed BigActors: " + a, debug)
                  proxies(bigActorID) ! a
                }
              }
            }
          }
        }
        case REMOTE_CONTROL_REQUEST(brr, bigActorID) => {
          Debug.println("got a ctr request " + brr,debug)
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
          Debug.println("got a snd request from " + bigActorID,debug)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostingRelation(senderID).name)
              val destHost = bigraph.getNode(hostingRelation(rcvID).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts " + hostingRelation(senderID).name + " and " +  hostingRelation(rcvID).name + " are connected.",debug)
                val rcv = proxies(rcvID)
                rcv ! msg
              } else {
                System.err.println("Hosts " + hostingRelation(senderID).name + " and " +  hostingRelation(rcvID).name + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case REMOTE_MIGRATION_REQUEST(newHostId, bigActorID) => {
          Debug.println("got a mgrt request from " + hostingRelation(bigActorID) + " to " +newHostId,debug)
          bigraphManager ! BIGRAPH_REQUEST
          react{
            case BIGRAPH_RESPONSE(bigraph) => {
              val currentHost = bigraph.getNode(hostingRelation(bigActorID).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts connected. Migrating...",debug)
                hostingRelation += bigActorID -> newHostId
              } else {
                System.err.println("Hosts " + hostingRelation(bigActorID) + " and " + newHostId + " are not connected.")
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
