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
        case x: BRR => {
          println("got a ctr request " + x + " from "+sender) //TODO - check if host is included on redex and reactum
          if (x.getNodes.contains(sender.asInstanceOf[BigActor].getHost)){
            brs.applyRules(List(x),2)
            println("New bigraph: " + brs)
          } else System.err.println("Host of " + sender.asInstanceOf[BigActor] + "is not included on redex/reactum of "+ x)
        }
        case x: (Node,Node) => {
          println("got a mgrt request from " + x._2 + " to " +x._1)
          if (!x._2.getNames.intersect(x._1.getNames).isEmpty){
            println("Hosts connected. Migrating...")
            reply(true)
          } else {
            reply(false)
            System.err.println("Hosts " + x._2 + " and " + x._1 + " are not connected.")
          }
        }
        case x: Message => {
          println("got a snd request " + x + " from "+sender)
          if (sender.asInstanceOf[BigActor].getHost == x.dest.getHost || !sender.asInstanceOf[BigActor].getHost.getNames.intersect(x.dest.getHost.getNames).isEmpty){
            println("BigActors are within connection. Sending message...")
            x.dest ! x.message
          } else System.err.println("BigActors " + sender + " and " + x.dest + " are not connected.")
        }
      }
    }
  }
}
