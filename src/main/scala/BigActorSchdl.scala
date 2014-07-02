package bigactors

import java.io.FileInputStream
import java.util.Properties

import scala.actors.{OutputChannel, Actor}
import edu.berkeley.eloi.bigraph._
import scala.collection.JavaConversions._
import edu.berkeley.eloi.bgm2java.Debug
import scala.collection.mutable.{ArrayBuffer, HashMap}

case object REQUEST_HOSTING_RELATION

object BigActorSchdl extends Actor {
  // configuration
  val prop = new Properties
  prop.load(new FileInputStream("config.properties"))
  val debug = prop.getProperty("debug").toBoolean

  private val hostRelation = new HashMap[OutputChannel[Any],Symbol]
  def act() {
    loop {
      react{
        case HOSTING_REQUEST(hostId) =>{
          Debug.println("[BigActorSchdl]:\t got a host request from " + sender + " to be hosted at "+hostId,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (bigraph.getPlaces.map(p=>p.getId).contains(hostId.name)) {
                Debug.println("[BigActorSchdl]:\t Hosting BigActor at host " + hostId,debug)
                hostRelation += requester -> hostId
                requester ! HOSTING_SUCCESSFUL
              }
              else {
                System.err.println("[BigActorSchdl]:\t BigActor cannot be hosted at " + hostId + ". Make sure host exists!")
                System.exit(0)
              }
            }
          }
        }
        case OBSERVATION_REQUEST(query) => {
          Debug.println("[BigActorSchdl]:\t got a obs request with query " + query + " from " + sender, debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive {
            case BIGRAPH_RESPONSE(bigraph) => {
              val hostId: String = hostRelation(requester).name
              val result = QueryInterpreter.evaluate(query, hostId, bigraph, hostRelation)
              result match {
                case Left(b) => {
                  Debug.println("[BigActorSchdl]:\t Observed Bigraph: " + b, debug)
                  requester ! b
                }
                case Right(a) => {
                  Debug.println("[BigActorSchdl]:\t Observed BigActors: " + a, debug)
                  requester ! a
                }
              }
            }
          }
        }
//        case OBSERVATION_REQUEST(query) => {
//          Debug.println("[BigActorSchdl]:\t got a obs request with query " + query + " from "+sender,debug)
//          val requester = sender
//          BigraphManager ! BIGRAPH_REQUEST
//          receive{
//            case BIGRAPH_RESPONSE(bigraph) => {
//              val hostId: String = hostRelation(requester).name
//              if(query.split('.').head == "hostedAt"){
//                val bigraphNodes = QueryInterpreter.evaluateString(query.substring(9),hostId,bigraph)
//                val bigactors = ArrayBuffer.empty[OutputChannel[Any]]
//                val reverseHostRelation = hostRelation groupBy {_._2} map {case (key,value) => (key, value.unzip._1)}
//                bigraphNodes.foreach{b =>
//                  val id = Symbol(b.getId.asInstanceOf[String])
//                  if (reverseHostRelation.contains(id)){
//                    reverseHostRelation(id).foreach{a =>
//                      println(a)
//                      bigactors+=a
//                    }
//                  }
//                }
//                if (!bigactors.isEmpty){
//                  Debug.println("[BigActorSchdl]:\t Observed BigActors: "+bigactors,debug)
//                  requester ! bigactors
//                }
//              }else{
//                val bigraphNodes = QueryInterpreter.evaluateString(query,hostId,bigraph)
//                Debug.println("[BigActorSchdl]:\t Observed Bigraph: "+bigraphNodes,debug)
//                requester ! bigraphNodes
//              }
//            }
//          }
//        }
        case CONTROL_REQUEST(brr) => {
          Debug.println("[BigActorSchdl]:\t got a ctr request " + brr,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              if (brr.getRedex.getNodes.contains(bigraph.getNode(hostRelation(requester).name))
                || brr.getReactum.getNodes.contains(bigraph.getNode(hostRelation(requester).name))){
                BigraphManager ! EXECUTE_BRR(brr)
              } else {
                System.err.println("[BigActorSchdl]:\t Host " + hostRelation(requester) + "is not included on redex/reactum of "+ brr)
                System.exit(0)
              }
            }
          }
        }
        case SEND_REQUEST(msg,rcv) => {
          Debug.println("[BigActorSchdl]:\t got a snd request from " + sender,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val senderHost = bigraph.getNode(hostRelation(requester).name)
              val destHost = bigraph.getNode(hostRelation(rcv).name)
              if (senderHost == destHost || !senderHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("[BigActorSchdl]:\t Hosts " + hostRelation(requester).name + " and " +  hostRelation(rcv).name + " are connected.",debug)
                rcv ! msg
              } else {
                System.err.println("[BigActorSchdl]:\t Hosts " + hostRelation(requester).name + " and " +  hostRelation(rcv).name + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case MIGRATION_REQUEST(newHostId) => {
          Debug.println("[BigActorSchdl]:\t got a mgrt request from " + hostRelation(sender) + " to " +newHostId,debug)
          val requester = sender
          BigraphManager ! BIGRAPH_REQUEST
          receive{
            case BIGRAPH_RESPONSE(bigraph) => {
              val currentHost = bigraph.getNode(hostRelation(requester).name)
              val destHost = bigraph.getNode(newHostId.name)
              if (!currentHost.getNames.intersect(destHost.getNames).isEmpty){
                Debug.println("[BigActorSchdl]:\t Hosts connected. Migrating...",debug)
                hostRelation += sender -> newHostId
              } else {
                System.err.println("[BigActorSchdl]:\t Hosts " + hostRelation(requester) + " and " + newHostId + " are not connected.")
                System.exit(0)
              }
            }
          }
        }
        case REQUEST_HOSTING_RELATION =>{
          sender ! hostRelation
        }
        case _ => println("[BigActorSchdl]:\t UNKNOWN REQUEST")
      }
    }
  }
  start
}
