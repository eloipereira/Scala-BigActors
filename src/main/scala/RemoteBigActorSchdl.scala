package bigactors

import scala.actors.{AbstractActor, Actor}
import scala.actors.remote.RemoteActor._
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug
import scala.collection.mutable.HashMap
import scala.actors.Actor._
import scala.actors.remote.Node
import java.util.Properties
import java.io.FileInputStream
import scala.collection.mutable


sealed trait RemoteBigActorSchdlAPI
case class REMOTE_HOSTING_REQUEST(hostId: Symbol, bigActorID: Symbol, bigActor:RemoteBigActor) extends RemoteBigActorSchdlAPI
case class REMOTE_HOSTING_REQUEST_(hostId: Symbol, bigActorID: Symbol) extends RemoteBigActorSchdlAPI
case object REMOTE_HOSTING_SUCCESSFUL extends RemoteBigActorSchdlAPI
case class REMOTE_OBSERVATION_REQUEST(query: String, bigActorID: Symbol) extends RemoteBigActorSchdlAPI
case class REMOTE_CONTROL_REQUEST(brr: BRR, bigActorID: Symbol) extends RemoteBigActorSchdlAPI
case class REMOTE_MIGRATION_REQUEST(newHostId: Symbol, bigActorID: Symbol) extends RemoteBigActorSchdlAPI
case class REMOTE_SEND_REQUEST(val msg: Message, val bigActorID: Symbol) extends RemoteBigActorSchdlAPI



object RemoteBigActorSchdl extends Actor with App {
  var debug = true

  private val hostRelation = new HashMap[Symbol,Symbol]
  private val bigActorIDRelation = new HashMap[Symbol,RemoteBigActor]

  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
  var bigraphManager: AbstractActor = RemoteBigraphManager
  if (remote) bigraphManager = select(Node(prop.getProperty("BigraphManagerIP"),prop.getProperty("BigraphManagerPort").toInt), 'bigraphManager)


  def act() {

    //more configuration
    if (remote){
      val port = prop.getProperty("BigActorSchdlPort").toInt
      val ip = prop.getProperty("BigActorSchdlIP")
      Debug.println("BigActorSchdl operating remotely at IP "+ ip + " and port "+ port.toInt,debug)
      //TODO - check if property actually matches with machine's IP
      alive(port)
      register('bigActorSchdl, self)
    } else {
      Debug.println("BigActorSchdl operating locally",debug)
    }


    loop {
      react{
        case REMOTE_HOSTING_REQUEST(hostId, bigActorID, bigActor) =>{
          Debug.println("got a host request from " + bigActorID + " to be hosted at "+hostId,debug)
          bigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.contains(new BigraphNode(hostId.name))) {
                Debug.println("Hosting BigActor at host " + hostId,debug)
                hostRelation += bigActorID -> hostId
                bigActorIDRelation += bigActorID -> bigActor
                bigActor ! REMOTE_HOSTING_SUCCESSFUL
              }
              else {
                System.err.println("BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
                System.exit(0)
              }
            }
          }
        }
        case REMOTE_HOSTING_REQUEST_(hostId, bigActorID) =>{
          Debug.println("got a host request from " + bigActorID + " to be hosted at "+hostId,debug)
          bigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.contains(new BigraphNode(hostId.name))) {
                Debug.println("Hosting BigActor at host " + hostId,debug)
                hostRelation += bigActorID -> hostId
                val bigActorPort = prop.getProperty(bigActorID.name + "Port").toInt
                val bigActorIP = prop.getProperty(bigActorID.name +"IP")
                val bigActor = select(Node(bigActorIP,bigActorPort), bigActorID)
                bigActor ! REMOTE_HOSTING_SUCCESSFUL
              }
              else {
                System.err.println("BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
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

              if (remote){
                val bigActorPort = prop.getProperty(bigActorID.name + "Port").toInt
                val bigActorIP = prop.getProperty(bigActorID.name +"IP")
                val bigActor = select(Node(bigActorIP,bigActorPort), bigActorID)
                bigActor ! obs
              }  else {
                reply(obs)
              }
            }
          }
        }
        case REMOTE_CONTROL_REQUEST(brr, bigActorID) => {
          Debug.println("got a ctr request " + brr,debug)
          bigraphManager ! BIGRAPH_REQUEST
          receive{
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
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostRelation(senderID).name)
              val destHost = bigraph.getNode(hostRelation(receiverID).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are connected.",debug)
                if (remote){
                  val bigActorPort = prop.getProperty(receiverID.name + "Port").toInt
                  val bigActorIP = prop.getProperty(receiverID.name +"IP")
                  val rcv = select(Node(bigActorIP,bigActorPort), receiverID)
                  rcv ! msg.message
                } else {
                  bigActorIDRelation(receiverID) ! msg.message
                }
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
          receive{
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
