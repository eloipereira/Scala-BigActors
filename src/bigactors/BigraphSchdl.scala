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
         case x: BRR => {println("got a ctr request " + x + " from "+sender) //TODO - check if host is included on redex and reactum
                         brs.applyRules(List(x),2)
                         println("New bigraph: " + brs)
                        }
         case x: Node => println("got a mgrt request " + x + " from "+sender) //TODO - implement migration
         case x: Message => {println("got a snd request " + x + " from "+sender)
                             if (sender.asInstanceOf[BigActor].host == x.dest.host || sender.asInstanceOf[BigActor].host.getNames.diff(x.dest.host.getNames).isEmpty){
                                 println("Yap, they're connected!")
                             } else System.err.println("BigActors " + sender + " and " + x.dest + " are not connected.")
                            }
       }
     }
  }
}
