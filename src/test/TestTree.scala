package test

import graphs.{Node, Tree}

object TestTree {
   def main (args : Array[String]){
     val tree: Tree[Char] = Node('a', List(Node('b'), Node('c', List(Node('e'), Node('f')))))
     println(tree)
   }
}
