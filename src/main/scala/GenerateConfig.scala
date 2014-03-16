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

  val config = "defaultSimple"

  config match {
    case "defaultSimple" => {
      prop.setProperty("RemoteBigActors","false")
      prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/simple.bgm")
      prop.setProperty("visualization","true")
      prop.setProperty("debug","true")
      prop.setProperty("log","false")
    }
    case "localhostRemoteSimple" => {
      prop.setProperty("RemoteBigActors","true")
      prop.setProperty("BigActorSchdlIP","localhost")
      prop.setProperty("BigActorSchdlPort","9010")
      prop.setProperty("BigraphManagerIP","localhost")
      prop.setProperty("BigraphManagerPort","9011")
      prop.setProperty("uav0IP","localhost")
      prop.setProperty("uav0Port","9012")
      prop.setProperty("uav1IP","localhost")
      prop.setProperty("uav1Port","9013")
      prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/simple.bgm")
      prop.setProperty("visualization","true")
      prop.setProperty("debug","true")
      prop.setProperty("log","false")
    }
    case "localhostRemoteICCPS" => {
      prop.setProperty("RemoteBigActors","true")
      prop.setProperty("BigActorSchdlIP","localhost")
      prop.setProperty("BigActorSchdlPort","9010")
      prop.setProperty("BigraphManagerIP","localhost")
      prop.setProperty("BigraphManagerPort","9011")
      prop.setProperty("searchOilIP","localhost")
      prop.setProperty("searchOilPort","9012")
      prop.setProperty("deployDrifterIP","localhost")
      prop.setProperty("deployDrifterPort","9013")
      prop.setProperty("bgmPath","/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/main/resources/ICCPS.bgm")
      prop.setProperty("visualization","true")
      prop.setProperty("debug","true")
      prop.setProperty("log","false")
    }
  }
  prop.store(new FileOutputStream("config.properties"),null)
}
