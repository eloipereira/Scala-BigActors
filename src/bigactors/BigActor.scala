package bigactors

import scala.actors.Actor
import Actor._
import edu.berkeley.eloi.bigraph._

abstract class BigActor (val bigactorId: String, host0Id: String, bigraphSchdl: Actor) extends Actor{
  private var hostId: String = host0Id

  val tmpActor = actor {
    bigraphSchdl ! ("HOSTING",bigactorId, host0Id)
    self.receive {
      case true => {
        this.hostId = host0Id
        println("BigActor successfuly hosted")
        exit()
      }
      case false => System.err.println("Hosting failed.")
    }
  }

  def observe(query: String, bigActorAddr: Actor) {
    val tmpActor = actor {
      bigraphSchdl ! ("OBSERVE",query, hostId, bigActorAddr: Actor)
      self.receive {
        case o: Observation => {
          println("observation received: " + o)
          bigActorAddr ! "hey"
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

  def migrate(newHostId: String) {
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


  def send(rcv:BigActor, message:Any): Unit = {
    val tmpActor = actor {
      //rcv.asInstanceOf[Actor] ! message
      bigraphSchdl ! ("SEND", new Message(rcv, message), hostId)
      self.receive {
        case true => {
          println("Sending message...")
          rcv.asInstanceOf[Actor] ! message  //TODO - this is failing to succeed for some unknown reason
        }
        case false => System.err.println("Send failed.")
      }
      exit()
    }
  }


//  override def !(msg:Any): Unit =
//  {
//    msg match {
//      case msg @ (rcv:BigActor, message:Any) => {
//        val tmpActor = actor {
//          bigraphSchdl ! ("SEND", new Message(rcv, message), hostId)
//          self.receive {
//            case true => {
//              println("Sending message...")
//              rcv.asInstanceOf[Actor] ! message  //TODO - this is failing to succeed for some unknown reason
//            }
//            case false => System.err.println("Send failed.")
//          }
//          exit()
//        }
//      }
//      case _ => super.!(msg)
//    }
//  }

  def getHostId: String = {
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
  def toString: String =  bigactorId + "@" + hostId
}
