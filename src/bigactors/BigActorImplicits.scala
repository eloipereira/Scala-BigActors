package bigactors

import BigActor._
import bigactors._

object BigActorImplicits {
  type BigActorSignature = (BigActorID, HostID)

  type Name = String
  implicit def Name2HostID(name: Name) = HostID(name)

  implicit def Name2BigActorID(name: Name) = BigActorID(name)

  class BigActorIDHelper(bigActorName: Name){
    def hosted_at(hostName:Name): BigActorSignature = (BigActorID(bigActorName),HostID(hostName))
  }

  implicit def Name2BigActorIDHelper(bigActorName: Name) = new BigActorIDHelper(bigActorName)


  class BigActorSignatureHelper(signature: BigActorSignature){
    def with_behavior(body: => Unit): BigActor = bigActor(signature._1)(signature._2)(body)
  }

  implicit def BigActorSignature2BigActorSignatureHelper(signature: BigActorSignature) = new  BigActorSignatureHelper(signature)
}
