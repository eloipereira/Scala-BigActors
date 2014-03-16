package bigactors.templates

import bigactors.{RemoteBigActor, Message, Observation, BigActor}

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
        case obs: Observation => clients.map(c => send(new Message(c,obs)))
      }
    }
  }
}
