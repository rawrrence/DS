package com.pool.networking

/**
 * Created by Lawrence on 4/15/15.
 */
@SerialVersionUID(1L)
class Message extends Serializable {
  var src : Int = -1
  var dest : Int = -1
  var mType : String = null
  var text : String = null
  var seqNum : Int = -1

  def this(src : Int, dest : Int, text : String) {
    this()
    this.src = src
    this.dest = dest
    this.text = text
  }

  def this(src : Int, dest : Int, text : String, mtype : String) {
    this()
    this.src = src
    this.dest = dest
    this.text = text
    this.mType = mtype
  }

  def setMessageType(mType : String) {
    this.mType = mType
  }
}
