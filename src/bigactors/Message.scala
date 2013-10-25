package bigactors

class Message (val receiverID: Symbol, val message: Any) {

  override
  def toString: String = "<" + receiverID + " <= " + message + ">"
}
