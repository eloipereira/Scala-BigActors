package bigactors

class Message (val sender: BigActor, val receiver: BigActor, val message: Any) {

  override
  def toString: String = "<" + receiver + " <= " + message + ">"
}
