package graphs

sealed abstract class Tree [+T]

case class Node[+T](value: T, children:List[Tree [T]]) extends Tree[T]{
  override def toString = {
    var result = "(" + value.toString
    for (t <- children){
      result += " " + t.toString
    }
    result += ")"
    result
  }
}

case object EmptyTree extends Tree[Nothing]{
  override def toString = "."
}

object Node {
  def apply[T](value: T): Node[T] = Node(value, List(EmptyTree))
}