package bigactors

import java.util.Properties
import java.io.FileOutputStream

/**
 * Created with IntelliJ IDEA.
 * User: eloipereira
 * Date: 10/30/13
 * Time: 10:59 AM
 * To change this template use File | Settings | File Templates.
 */
object GenerateConfig extends App {
  val prop = new Properties()

  val config = "localhost"

  config match {
    case "default" => {
      prop.setProperty("RemoteBigActors","false")
    }
    case "localhost" => {
      prop.setProperty("RemoteBigActors","true")
      prop.setProperty("BigActorSchdlIP","localhost")
      prop.setProperty("BigActorSchdlPort","9010")
      prop.setProperty("BigraphManagerIP","localhost")
      prop.setProperty("BigraphManagerPort","9011")
      prop.setProperty("uav0IP","localhost")
      prop.setProperty("uav0Port","9012")
      prop.setProperty("uav1IP","localhost")
      prop.setProperty("uav1Port","9013")
    }
  }
  prop.store(new FileOutputStream("config.properties"),null)
}
