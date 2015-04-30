package com.pool.networking

/**
 * Created by StevW on 4/20/15.
 */

@SerialVersionUID(1L)
class BootstrapMessage(n : Node, f : Int) extends Serializable {
  var node : Node = n
  var flag : Int = f  //0 for init, 1 for exit, 2 for update, 3 for heartbeat
}
