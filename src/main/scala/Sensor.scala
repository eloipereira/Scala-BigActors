package bigactors.remote.templates

import bigactors.Observation
import bigactors.remote._


/**
 * Created with IntelliJ IDEA.
 * User: eloipereira
 * Date: 11/19/13
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
class Sensor(sensorID: Symbol, sensorHostID: Symbol, query: String, clients: List[Symbol]) extends RemoteBigActor(sensorID, sensorHostID) {
  def behavior{
    loop{
      observe(query)
      receive{
        case obs: Observation => clients.map(c => sendMsg(obs,c))
      }
    }
  }
}
