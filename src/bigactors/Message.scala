package bigactors

class Message (val dest: BigActor, val message: Any) {

  override
  def toString: String = "<" + dest + " <= " + message + ">"
}
