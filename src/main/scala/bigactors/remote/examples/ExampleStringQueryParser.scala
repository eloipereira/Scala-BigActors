package bigactors.remote.examples

import bigactors.QueryInterpreter.QueryParser

/**
 * Created by eloi on 8/12/14.
 */
object ExampleStringQueryParser extends QueryParser with App{
  println(parseAll(query,"children.parent.host"))
}
