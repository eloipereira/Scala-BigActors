package bigactors

import scala.actors.{AbstractActor, Actor}
import edu.berkeley.eloi.bigraph._
import scala.actors.Actor._
import scala.actors.remote._
import scala.actors.remote.RemoteActor._
import edu.berkeley.eloi.bgm2java.Debug
import java.util.{UUID, Properties}
import java.io.FileInputStream
import java.net._


trait RemoteBigActorTrait{
  val bigActorID: Symbol
  var bigActorSchdl: AbstractActor

  def observe(query: String) = {
    bigActorSchdl ! REMOTE_OBSERVATION_REQUEST(query, bigActorID)
  }

  def control(brr: BRR){
    bigActorSchdl ! REMOTE_CONTROL_REQUEST(brr, bigActorID)
  }

  def migrate(newHostId: Symbol){
    bigActorSchdl ! REMOTE_MIGRATION_REQUEST(newHostId, bigActorID)
  }

  def send(msg: Message){
    bigActorSchdl ! REMOTE_SEND_REQUEST(msg, bigActorID)
  }

}


abstract class RemoteBigActor(val bigActorID: Symbol, var hostID: Symbol) extends Actor with RemoteBigActorTrait  {

  def this(hostID: Symbol) = this(Symbol(UUID.randomUUID().toString), hostID)

  val debug: Boolean = true

  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
  if (!remote){
    System.err.println("Configuration file not set for remote bigActors",debug)
    System.exit(0)
  }

  var bigActorSchdl =  select(Node(prop.getProperty("BigActorSchdlIP"),prop.getProperty("BigActorSchdlPort").toInt), 'bigActorSchdl)

  def behavior()

  def act() = {
    val port = prop.getProperty("BigActorsPort").toInt
    val localhost = InetAddress.getLocalHost
    val ip = localhost.getHostAddress

    alive(port)
    register(this.bigActorID, self)
    bigActorSchdl ! REMOTE_HOSTING_REQUEST(this.bigActorID, ip,port,hostID)
    receive{
      case REMOTE_HOSTING_SUCCESSFUL => Debug.println("Remote BigActor " + this.bigActorID +  " hosted at " + hostID + " with IP " + ip + " and port " + port ,debug)
    }

    behavior
  }
}



object RemoteBigActor{

  def remoteBigActor(bigActorID: Symbol)(hostId: Symbol)(body: => Unit): RemoteBigActor = new RemoteBigActor(bigActorID,hostId){
    override def behavior() = body
    this.start
  }

  def remoteBigActor(hostId: Symbol)(body: => Unit): RemoteBigActor = new RemoteBigActor(hostId){
    override def behavior() = body
    this.start
  }

}



object RemoteBigActorImplicits {
  type BigActorSignature = (Symbol, Symbol)
  type Name = String
  type MessageHeader = (Symbol,Any)

  implicit def Name2Symbol(name: Name) = Symbol(name)
  implicit def Name2BigActorIDHelper(bigActorName: Name) = new RemoteBigActorIDHelper(bigActorName)
  implicit def BigActorSignature2BigActorHelper(signature: BigActorSignature) = new  BigActorHelper(signature)
  implicit def MessageHeader2MessageHelper(msgHeader: MessageHeader) = new MessageHelper(msgHeader)
  implicit def String2BigraphReactionRule(term: String) = new BRR(term)
  implicit def String2Node(nodeName: String) = new BigraphNode(nodeName)

  class RemoteBigActorIDHelper(bigActorName: Name) extends RemoteBigActorTrait{

    val bigActorID = Symbol(bigActorName)

    // configuration
    val prop = new Properties
    prop.load(new FileInputStream("config.properties"))
    val remote: Boolean = prop.getProperty("RemoteBigActors").toBoolean
    var bigActorSchdl: AbstractActor = select(Node(prop.getProperty("BigActorSchdlIP"),prop.getProperty("BigActorSchdlPort").toInt), 'bigActorSchdl)

    def hosted_at(hostName:Name): BigActorSignature = (bigActorID,Symbol(hostName))
    def send_message(msg: Any): MessageHeader = (bigActorID,msg)
  }

  class BigActorHelper(signature: BigActorSignature){
    def with_behavior (body : => Unit): RemoteBigActor = new RemoteBigActor(signature._1,signature._2) {
      def behavior() = body
      start
    }
  }

  class MessageHelper(msgHeader: MessageHeader) {
    def to(rcv: Name) = msgHeader._1.name send(new Message(rcv,msgHeader._2))
  }

}
