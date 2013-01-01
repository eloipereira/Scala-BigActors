package bigactors

import actors.Actor
import java.util.Observable
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._

class BigraphSchdl(brs0 : BRS) extends Actor{
  var brs: BRS = brs0
  def act() {
    println("Initial bigraph: " + brs)
     loop {
       react{
         case x: Query => println("got a obs request " + x + " from "+sender.toString)
         case x: BRR => {println("got a ctr request " + x + " from "+sender.toString)
                         brs.applyRules(List(x),2)
                         println("New bigraph: " + brs)
                        }
         case x: Node => println("got a mgrt request " + x + " from "+sender.toString)
       }
     }
  }
}
