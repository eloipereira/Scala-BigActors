package bigactors.templates

import bigactors.{Message, Observation, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR

/**
 * Created with IntelliJ IDEA.
 * User: eloipereira
 * Date: 11/19/13
 * Time: 1:11 PM
 * To change this template use File | Settings | File Templates.
 */
class Controller(controllerID: Symbol, controllerHostID: Symbol, brr: BRR) extends RemoteBigActor(controllerID, controllerHostID) {
  val test = true
  def behavior{
  }
}
