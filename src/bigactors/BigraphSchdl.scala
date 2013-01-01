package bigactors

import actors.Actor
import java.util.Observable
import edu.berkeley.eloi.bigraph._

class BigraphSchdl(brs0 : BRS) extends Actor{
  var brs: BRS = brs0
  def act() {
     loop {
       react{
         case x: Query => println("got a obs request " + x + " from "+sender.toString)
         case x: BRR => println("got a ctr request " + x + " from "+sender.toString)
         case x: Node => println("got a mgrt request " + x + " from "+sender.toString)
       }
     }
  }
}
