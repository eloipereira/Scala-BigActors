package bigactors

import actors.Actor
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._

class BigraphSchdl(brs0 : BRS) extends Actor{
  var brs: BRS = brs0
  def act() {
    println("Initial bigraph: " + brs)
    loop {
      react{
        case x: Query => println("got a obs request " + x + " from "+sender) //TODO - implement query language
        case x@(h:Node,r:BRR) => {
          println("got a ctr request " + r)
          if (r.getNodes.contains(h)){
            brs.applyRules(List(r),2)
            println("New bigraph: " + brs)
          } else System.err.println("Host " + h + "is not included on redex/reactum of "+ r)
        }
        case x@(m:Message,h:Node) => {
          println("got a snd request " + x + " from "+sender)
          if (h == m.dest.getHost || !h.getNames.intersect(m.dest.getHost.getNames).isEmpty){
            println("BigActors are within connection.")
            reply(true)
          } else System.err.println("BigActors " + h + " and " + m.dest + " are not connected.")
        }
        case x@(h:Node,host:Node) => {
          println("got a mgrt request from " + host + " to " +h)
          if (!host.getNames.intersect(h.getNames).isEmpty){
            println("Hosts connected. Migrating...")
            reply(true)
          } else {
            reply(false)
            System.err.println("Hosts " + host + " and " + h + " are not connected.")
          }
        }
      }
    }
  }
}
