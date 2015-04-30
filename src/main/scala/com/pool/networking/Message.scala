package com.pool.networking

/**
 * Created by Lawrence on 4/15/15.
 */
@SerialVersionUID(1L)
class Message extends Serializable {
  var src : Int = -1
  var dest : Int = -1
  var mType : String = null
  var title : String = ""
  var body : String = ""
  var seqNum : Int = -1
  var phone : String = ""

  def this(src : Int, dest : Int, title : String, body : String) {
    this()
    this.src = src
    this.dest = dest
    this.title = title
    this.body = body
  }

  def this(src : Int, dest : Int, title : String, body : String, mtype : String) {
    this()
    this.src = src
    this.dest = dest
    this.title = title
    this.body = body
    this.mType = mtype
  }

  def setMessageType(mType : String) {
    this.mType = mType
  }
}
