package bigactors.akkaBigActors

import akka.actor.Actor
import akka.event.Logging
import bigactors.{BIGRAPH_REQUEST, BIGRAPH_RESPONSE, EXECUTE_BRR}
import edu.berkeley.eloi.bigraph.BRS

import scala.collection.JavaConversions._

/**
 * Created by eloi on 03-07-2014.
 */
class AkkaBigraphManager( bgmPath: String, visualization: Boolean = false, log: Boolean = false) extends Actor {

  val logging = Logging(context.system, this)
  var brs: BRS = new BRS(bgmPath,log,visualization)

  def receive = {
    case EXECUTE_BRR(brr) => {
      logging.info("[BigraphManager]:\t Old bigraph: " + brs)
      brs.applyRules(List(brr),2)
      logging.info("[BigraphManager]:\t New bigraph: " + brs)
    }
    case BIGRAPH_REQUEST => {
      logging.info("[BigraphManager]:\t Sending bigraph: " + brs)
      sender ! BIGRAPH_RESPONSE(brs.getBigraph)
    }
  }

}
