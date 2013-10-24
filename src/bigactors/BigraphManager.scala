package bigactors

import edu.berkeley.eloi.bigraph.{Control, BRR, Bigraph, BRS}
import scala.collection.mutable.ArrayBuffer
import java.util
import scala.collection.JavaConversions._

/**
 * Created with IntelliJ IDEA.
 * User: eloi
 * Date: 6/27/13
 * Time: 10:26 PM
 * To change this template use File | Settings | File Templates.
 */
object BigraphManager {

  var brs: BRS = new BRS("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true,true)

  //var brs: BRS = new BRS(new util.ArrayList[Control](),new util.ArrayList[String](), new Bigraph(), new util.ArrayList[BRR](), false)
  var brrQueue = ArrayBuffer[BRR]()

  def enqueueBRR(brr: BRR){
    brrQueue += brr
  }

  def getBRS: BRS = brs

  def setBRS(new_brs: BRS){
    brs = new_brs
  }

}