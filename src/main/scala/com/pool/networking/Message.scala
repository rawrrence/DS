package com.pool.networking

/**
 * Created by Lawrence on 4/15/15.
 */
class Message extends java.io.Serializable {
  var src : Int = -1
  var dest : Int = -1
  var text : String = null

  def this(src : Int, dest : Int, text : String) {
    this()
    this.src = src
    this.dest = dest
    this.text = text
  }

}
