package bigactors
package remote
/**
 * Created with IntelliJ IDEA.
 * User: eloipereira
 * Date: 4/29/13
 * Time: 10:22 PM
 * To change this template use File | Settings | File Templates.
 */
abstract class Structure

case class HOST(bigActor: RemoteBigActor) extends Structure

case class PARENT_HOST(bigActor: RemoteBigActor) extends Structure

case class CHILDREN_PARENT_HOST(bigActor: RemoteBigActor) extends Structure

