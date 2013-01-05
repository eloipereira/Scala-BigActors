package bigactors

import scala.actors.Actor
import Actor._
import edu.berkeley.eloi.bigraph._

abstract class BigActor (val name: String, host0: Node, bigraphSchdl: Actor) extends Actor{
  private var host: Node = host0

  def observe(q: Query) {
    val tmpActor = actor {
      bigraphSchdl ! q
      exit()
    }
  }
  def control(u: BRR) {
    val tmpActor = actor {
      bigraphSchdl ! (u,host)
      exit()
    }
  }

  def migrate(h: Node) {
    val tmpActor = actor {
      bigraphSchdl ! (h,host)
      self.receive {
        case b: Boolean => if (b) {
          this.host = h
          println("Migration succeeded")
          exit()
        } else System.err.println("Migration failed.")
      }
    }
  }


  override def !(msg:Any): Unit =
  {
    msg match {
      case msg @ (rcv:BigActor, message:Any) => {
        val tmpActor = actor {
          bigraphSchdl ! (new Message(rcv, message),host)
          self.receive {
            case true => {
              println("Sending message...")
              rcv.asInstanceOf[Actor] ! message
              exit()
            }
            case false => System.err.println("Send failed.")
          }
        }
      }
      case _ => super.!(msg)
    }
  }


  //def send(dest: BigActor, message: Any) {
  //  bigraphSchdl ! new Message(dest, message)
  //}

  def getHost: Node = host

  override
  def toString: String =  name + "@" + host
}
