package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import collection.mutable

class BigraphSchdl(brs0 : BRS) extends Actor{
  var brs: BRS = brs0
  val hostRelation = new mutable.HashMap[String,String]()

  def act() {
    println("Initial bigraph: " + brs)
    loop {
      react{
        case x@("HOSTING", bigActorId: String,hostId : String)=> {
          println("Hosting BigActor " + bigActorId + " at host " + hostId)
          if (brs.getBigraph.getNodes.contains(new Node(hostId))) {
            hostRelation.+=((bigActorId,hostId))
            reply(true)
          }
          else {
            System.err.println("BigActor " + bigActorId + " cannot be hosted at " + hostId + ". Make sure host exists!")
            reply(false)
          }
        }
        case x@("OBSERVE", query: Query) => println("got a obs request " + x + " from "+sender) //TODO - implement query language
        case x@("CONTROL", r:BRR, hostId:String) => {
          println("got a ctr request " + r)
          if (r.getNodes.contains(brs.getBigraph.getNode(hostId))){
            brs.applyRules(List(r),2)
            println("New bigraph: " + brs)
          } else System.err.println("Host " + hostId + "is not included on redex/reactum of "+ r)
        }
        case x@("SEND", m:Message,senderHostId:String) => {
          println("got a snd request " + x + " from "+sender)
          val senderHost = brs.getBigraph.getNode(senderHostId)
          val destHost = brs.getBigraph.getNode(m.dest.getHostId)
          if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
            println("BigActors are within connection.")
            reply(true)
          } else System.err.println("BigActors are not connected.")
        }
        case x@("MIGRATE", currentHostId:String, destHostId:String) => {
          println("got a mgrt request from " + currentHostId + " to " +destHostId)
          val currentHost = brs.getBigraph.getNode(currentHostId)
          val destHost = brs.getBigraph.getNode(destHostId)
          if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
            println("Hosts connected. Migrating...")
            reply(true)
          } else {
            reply(false)
            System.err.println("Hosts " + currentHostId + " and " + destHostId + " are not connected.")
          }
        }
        case x@("GET_HOST",hostId: String) => {
          val idx = brs.getBigraph.getNodes.indexOf(new Node(hostId))
          reply(brs.getBigraph.getNodes.get(idx))
        }
      }
    }
  }
}
