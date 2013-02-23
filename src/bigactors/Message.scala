package bigactors

class Message (val senderID: BigActorID, val receiverID: BigActorID, val message: Any) {

  override
  def toString: String = "<" + receiverID + " <= " + message + ">"
}
