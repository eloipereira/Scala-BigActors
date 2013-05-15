package bigactors

import edu.berkeley.eloi.bigraph.BRS

object Initializer {
//  lazy val scheduler = {
//    val brs: BRS = new BRS("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/simple.bgm",true,true)
//        val scheduler_ = new BigraphSchdl(brs)
//        scheduler_.start()
//        scheduler_
//  }

  lazy val scheduler = {
      val brs: BRS = new BRS("/Users/eloipereira/Dropbox/IDEAWorkspace/BigActors/src/examples/qualsEx1.bgm",true,true)
          val scheduler_ = new BigraphSchdl(brs)
          scheduler_.start()
          scheduler_
    }
}
