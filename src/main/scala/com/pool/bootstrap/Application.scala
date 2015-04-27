package com.pool.bootstrap

import java.io.InterruptedIOException

object Application {
  def main(args:Array[String]) {
    if (args.length != 1) {
      println("bootstrap requires 1 arguments: $BOOTSTRAP_PORT")
      return
    }

    var bootstrap : Bootstrap = new Bootstrap(args(0))

    while(true) {
      try {
        Thread.sleep(10000);
      } catch {
        case e: InterruptedIOException =>;
      }
    }
  }

}
