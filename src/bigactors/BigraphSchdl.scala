package bigactors

import scala.actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug
import scala.collection.mutable.HashMap

class BigraphSchdl(brs0 : BRS) extends Actor{
  private var brs: BRS = brs0
  var debug = false

  private val hostRelation = new HashMap[BigActorID,HostID]
  private val addressesRelation = new HashMap[BigActorID,BigActor]

  def getBRS = brs

  def act() {
    Debug.println("Initial bigraph: " + brs,debug)
    loop {
      react{
        case x@("HOSTING",hostId : HostID, bigActorId: BigActorID, bigActorAddr: BigActor)=> {
          if (brs.getBigraph.getPlaces.contains(new Node(hostId.name))) {
            Debug.println("Hosting BigActor at host " + hostId,debug)
            hostRelation += bigActorId -> hostId
            addressesRelation +=  bigActorId -> bigActorAddr
            bigActorAddr.start()
          }
          else {
            System.err.println("BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
            System.exit(0)
          }
        }
        case x@("OBSERVE", query: String, bigActorId: BigActorID) => {
          Debug.println("got a obs request " + x + " from "+sender,debug)
          val host = brs.getBigraph.getNode(hostRelation(bigActorId).name)
          val obs = new Observation(SimpleQueryCompiler.generate(query,host,brs.getBigraph))
          Debug.println("Observation: "+obs,debug)
          reply(("OBSERVATION_SUCCESSFUL",obs))
        }
        case x@("CONTROL", r:BRR,  bigActorId: BigActorID) => {
          Thread.sleep(3000)
          Debug.println("got a ctr request " + r,debug)
          if (r.getNodes.contains(brs.getBigraph.getNode(hostRelation(bigActorId).name))){
            brs.applyRules(List(r),2)
            Debug.println("New bigraph: " + brs,debug)
          } else {
            System.err.println("Host " + hostRelation(bigActorId) + "is not included on redex/reactum of "+ r)
            System.exit(0)
          }
        }
        case x@("SEND", msg: Message) => {
          val senderID = msg.senderID
          val receiverID = msg.receiverID
          Debug.println("got a snd request " + x,debug)
          val senderHost = brs.getBigraph.getNode(hostRelation(senderID).name)
          val destHost = brs.getBigraph.getNode(hostRelation(receiverID).name)
          if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
            Debug.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are connected.",debug)
            addressesRelation(receiverID) ! ("SEND_SUCCESSFUL",msg)
          } else {
            System.err.println("Hosts " + hostRelation(senderID).name + " and " +  hostRelation(receiverID).name + " are not connected.")
            System.exit(0)
          }
        }
        case x@("MIGRATE", bigActorId: BigActorID, destHostId:HostID) => {
          Debug.println("got a mgrt request from " + hostRelation(bigActorId) + " to " +destHostId,debug)
          val currentHost = brs.getBigraph.getNode(hostRelation(bigActorId).name)
          val destHost = brs.getBigraph.getNode(destHostId.name)
          if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
            Debug.println("Hosts connected. Migrating...",debug)
            hostRelation += bigActorId -> destHostId
          } else {
            System.err.println("Hosts " + hostRelation(bigActorId) + " and " + destHostId + " are not connected.")
            System.exit(0)
          }
        }
      }
    }
  }
}
