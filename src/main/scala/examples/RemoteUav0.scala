package bigactors
package remote

import edu.berkeley.eloi.bigraph.{BRR, Place}


object RemoteUav0 extends RemoteBigActor( Symbol("uav0"), Symbol("u0")) with App{
  def behavior() {
    observe(Children(Parent(Host)))
    react{
      case obs: Array[Place] => {
        println("New observation for uav0: "+ obs)
        Thread.sleep(10000)
        sendMsg("Hello I'm a BigActor!",Symbol("uav1"))
        control(new BRR("l0_Location[x].(u0_UAV[z] | $0) | l1_Location[x].$1 -> l0_Location[x].$0 | l1_Location[x].(u0_UAV[z] | $1)"))
        migrate(Symbol("u1"))
        observe(Host)
        react{
          case obs: Array[Place] => println("New observation for uav0: "+ obs)
        }
      }
    }
  }
  start
}


