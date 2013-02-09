package bigactors

import scala.actors.Actor
import Actor._
import edu.berkeley.eloi.bigraph._

abstract class BigActor (host0Id: HostID) extends Actor{
  private var hostId: HostID = host0Id
  private var bigraphSchdl: Actor = null



  def setScheduler (bigraphSchdl: Actor) = {
     this.bigraphSchdl = bigraphSchdl
    val tmpActor = actor {
        bigraphSchdl ! ("HOSTING", host0Id)
        self.receive {
          case true => {
            this.hostId = host0Id
            println("BigActor successfuly hosted")
            exit()
          }
          case false => System.err.println("Hosting failed.")
        }
      }
  }

  def observe(query: String) {
    val bigActorAddr: Actor = this
    val tmpActor = actor {
      bigraphSchdl ! ("OBSERVE",query, hostId, bigActorAddr: Actor)
      self.receive {
        case o: Observation => {
          println("observation received: " + o)   //TODO - need to send observation back to the bigActor
          //bigActorAddr ! o
        }
      }
      exit()
    }
  }
  def control(u: BRR) {
    val tmpActor = actor {
      bigraphSchdl ! ("CONTROL",u,hostId)
      exit()
    }
  }

  def migrate(newHostId: HostID) {
    val tmpActor = actor {
      bigraphSchdl ! ("MIGRATE",hostId,newHostId)
      self.receive {
        case b: Boolean => if (b) {
          this.hostId = newHostId
          println("Migration succeeded")
          exit()
        } else System.err.println("Migration failed.")
      }
    }
  }


  override def !(msg:Any)
  {
    val rec = this
    msg match {
      case m: Message => {
        actor {
          bigraphSchdl ! ("SEND", m)
          self.react {
            case true =>
              println("Sending message...")
              rec.superBang(m)
              println("Sending done!")
            case false => {
              System.err.println("Send failed.")
              System.exit(0)
            }
          }
          exit()
        }
      }
      case _ => {
        println("Super")
        super.!(msg)
      }
    }
  }

  def superBang (msg:Any) {
    println("Super2")
    super.!(msg)
  }

  def getHostId: HostID = {
    this.hostId
  }

  def getHost: Node = {
    var gotIt = false
    var host: Node = null
    val tmpActor = actor {
      bigraphSchdl ! ("GET_HOST",hostId)
      self.receive {
        case n: Node => {
          gotIt = true
          host = n
        }
      }
    }
    while (!gotIt){}
    return host
  }

  override
  def toString: String =  "Bigactor @" + hostId
}
