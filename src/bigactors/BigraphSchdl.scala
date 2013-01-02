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
         case x: Query => println("got a obs request " + x + " from "+sender)
         case x: BRR => {println("got a ctr request " + x + " from "+sender)
                         brs.applyRules(List(x),2)
                         println("New bigraph: " + brs)
                        }
         case x: Node => println("got a mgrt request " + x + " from "+sender)
         case x: Message => {println("got a snd request " + x + " from "+sender)
                             if (sender.asInstanceOf[BigActor].host == x.dest.host || sender.asInstanceOf[BigActor].host.getNames.diff(x.dest.host.getNames).isEmpty){
                                 println("Yap, they're connected!")
                             } else println("Not connected!")
                            }
       }
     }
  }
}
