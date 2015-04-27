package com.pool

import java.io.BufferedReader
import java.util

import com.pool.networking.{MessagePasser, Message}

/**
 * Created by Lawrence on 4/16/15.
 */
object Application {
  def main(args:Array[String]) {
    if (args.length != 4) {
      println("Application requires 4 arguments: $LOCAL_NAME, $LOCAL_PORT, $BOOTSTRAP_ADDRESS, $BOOTSTRAP_PORT")
      return
    }

    var mp : MessagePasser = new MessagePasser(args(0), args(1), args(2), args(3))

    while (true) {
      val ln = readLine()
      val input = ln.trim.split(" ")
      if (input(0).equalsIgnoreCase("S")) {
        if (input(1).equals("multicast")) {
          mp.broadcast(parseMessage(ln))
        } else {
            mp.send(parseMessage(ln), input(1).toInt)
        }
      } else {
        println("Invalid input")
      }
    }
  }

  def parseMessage(input : String): Message = {
    val secondSpace = input.indexOf(" ", input.indexOf(" ") + 1)
    var msg : Message = new Message()
    msg.text = input.substring(secondSpace + 1)
    return msg
  }
}