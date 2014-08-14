package bigactors.remote.templates

import bigactors._
import bigactors.remote._
import edu.berkeley.eloi.bigraph.Place


/**
 * Created with IntelliJ IDEA.
 * User: eloipereira
 * Date: 11/19/13
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
class Sensor(sensorID: Symbol, sensorHostID: Symbol, query: Query, clients: List[Symbol]) extends RemoteBigActor(sensorID, sensorHostID) {
  def behavior{
    loop{
      observe(query)
      receive{
        case obs: Array[Place] => clients.map(c => sendMsg(obs,c))
      }
    }
  }
}
