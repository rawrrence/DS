package com.pool.networking

/**
 * Created by StevW on 4/20/15.
 */
class BootstrapMessage(n : Node, f : Int) extends java.io.Serializable {
  var node : Node = n
  var flag : Int = f  //0 for init, 1 for exit
}
