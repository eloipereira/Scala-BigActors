package bigactors

class Message (val receiverID: Symbol, val message: Any) extends Serializable {

  override
  def toString: String = "<" + receiverID + " <= " + message + ">"


}
