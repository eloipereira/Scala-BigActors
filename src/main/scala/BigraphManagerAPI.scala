package bigactors

import edu.berkeley.eloi.bigraph.{Bigraph, BRR}

/**
 * Created by eloipereira on 3/24/14.
 */
sealed trait BigraphManagerAPI extends Serializable
case class EXECUTE_BRR(brr: BRR) extends BigraphManagerAPI
case object BIGRAPH_REQUEST extends BigraphManagerAPI
case class BIGRAPH_RESPONSE(bigraph: Bigraph) extends BigraphManagerAPI
