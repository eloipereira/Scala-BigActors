package bigactors.akkaBigActors

import akka.actor.Actor
import akka.event.Logging
import bigactors.{BIGRAPH_REQUEST, BIGRAPH_RESPONSE, EXECUTE_BRR}
import edu.berkeley.eloi.bigraph.{BRR, Bigraph}

/**
 * Created by eloi on 03-07-2014.
 */

abstract class BigraphManager extends Actor{
  val logging = Logging(context.system, this)

  def executeBRR(brr: BRR): Unit
  def getBigraph: Bigraph

  def receive = {
    case EXECUTE_BRR(brr) =>
      logging.info("[BigraphManager]:\t Execute BRR: " + brr)
      executeBRR(brr)
    case BIGRAPH_REQUEST => {
      logging.info("[BigraphManager]:\t Sending bigraph: " + brs)
      sender ! BIGRAPH_RESPONSE(getBigraph)
    }
  }
}
