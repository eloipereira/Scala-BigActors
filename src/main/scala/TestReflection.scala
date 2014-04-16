package bigactors

import scala.actors.Actor
import scala.reflect.runtime.universe._


/**
 * Created by eloipereira on 3/30/14.
 */
object TestReflection extends App {
  val a = new MyActor("eloi")

  val m = runtimeMirror(a.getClass.getClassLoader)

}



class MyActor(var myName: String) extends Actor{
  def act{
    react{
      case x: String => myName = x
    }
  }
}
