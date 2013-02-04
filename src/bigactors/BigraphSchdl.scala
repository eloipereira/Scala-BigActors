package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import collection.mutable

class BigraphSchdl(brs0 : BRS) extends Actor{
  var brs: BRS = brs0


  def act() {
    println("Initial bigraph: " + brs)
    loop {
      react{
        case x@("HOSTING",hostId : HostID)=> {
          println("Hosting BigActor at host " + hostId)
          if (brs.getBigraph.getNodes.contains(new Node(hostId.name))) {
            reply(true)
          }
          else {
            System.err.println("BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
            reply(false)
          }
        }
        case x@("OBSERVE", query: String, hostId:HostID, bigActorAddr: Actor) => {
          println("got a obs request " + x + " from "+sender)
          val host = brs.getBigraph.getNode(hostId.name)
          val obs = new Observation(SimpleQueryCompiler.generate(query,host,brs.getBigraph))
          println("Observation: "+obs)
          reply(obs)
        }
        case x@("CONTROL", r:BRR, hostId:HostID) => {
          println("got a ctr request " + r)
          if (r.getNodes.contains(brs.getBigraph.getNode(hostId.name))){
            brs.applyRules(List(r),2)
            println("New bigraph: " + brs)
          } else System.err.println("Host " + hostId + "is not included on redex/reactum of "+ r)
        }
        case x@("SEND", m:Message,senderHostId:HostID) => {
          println("got a snd request " + x + " from "+sender)
          val senderHost = brs.getBigraph.getNode(senderHostId.name)
          val destHost = brs.getBigraph.getNode(m.dest.getHostId.name)
          if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
            println("BigActors are within connection.")
            reply(true)
          } else System.err.println("BigActors are not connected.")
        }
        case x@("MIGRATE", currentHostId:HostID, destHostId:HostID) => {
          println("got a mgrt request from " + currentHostId + " to " +destHostId)
          val currentHost = brs.getBigraph.getNode(currentHostId.name)
          val destHost = brs.getBigraph.getNode(destHostId.name)
          if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
            println("Hosts connected. Migrating...")
            reply(true)
          } else {
            reply(false)
            System.err.println("Hosts " + currentHostId + " and " + destHostId + " are not connected.")
          }
        }
        case x@("GET_HOST",hostId: HostID) => {
          val idx = brs.getBigraph.getNodes.indexOf(new Node(hostId.name))
          reply(brs.getBigraph.getNodes.get(idx))
        }
      }
    }
  }
}
