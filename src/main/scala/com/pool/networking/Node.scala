package com.pool.networking

/**
 * Created by Lawrence on 4/15/15.
 */
class Node extends Serializable {
  var id : Int = -1
  var name : String = null
  var ip : String = null
  var port : Integer = null
  var locationX : Double = 0.0
  var locationY : Double = 0.0

  def this(name : String, ip : String, port : Integer) {
    this()
    this.id = id
    this.name = name
    this.ip = ip
    this.port = port
  }

}
