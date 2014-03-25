import bigactors.{Observation, RemoteBigActor}
import edu.berkeley.eloi.bigraph.BRR
;

object RemoteUav0 extends RemoteBigActor( Symbol("uav0"), Symbol("u0")) with App{
  def behavior() {
    observe("children.parent.host")
    react{
      case obs: Observation => {
        println("New observation for uav0: "+ obs)
        Thread.sleep(10000)
        sendMsg("Hello I'm a BigActor!",Symbol("uav1"))
        control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
        migrate(Symbol("u1"))
        observe("host")
        react{
          case obs: Observation => println("New observation for uav0: "+ obs)
        }
      }
    }
  }
  start
}



