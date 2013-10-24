package bigactors

class Message (val senderID: Symbol, val receiverID: Symbol, val message: Any) {

  override
  def toString: String = "<" + receiverID + " <= " + message + ">"
}
