
import bigactors.{Observation, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR
;

object TestRemoteUav0 extends App{

 val uav1 = new RemoteBigActor( Symbol("uav1"), Symbol("u1")){
    def behavior() {
      observe("children.parent.host")
      loop {
        react {
          case obs: Observation => println("New observation for uav1: " + obs)
          case msg: Any => println("New mail for uav1: " + msg)
        }
      }
    }
  }
  uav1.start

  val uav0 = new RemoteBigActor( Symbol("uav0"), Symbol("u0")){
    def behavior() {
      observe("children.parent.host")
      react{
        case obs: Observation => {
          println("New observation for uav0: "+ obs)
          sendMsg("Hello I'm a BigActor!",Symbol("uav1"))
          Thread.sleep(5000)
          control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
          migrate(Symbol("u1"))
          observe("host")
          react{
            case obs: Observation => println("New observation for uav0: "+ obs)
          }
        }
      }
    }
  }
  uav0.start

}
