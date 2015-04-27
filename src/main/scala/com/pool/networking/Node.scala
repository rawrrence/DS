package com.pool.networking

/**
 * Created by Lawrence on 4/15/15.
 */
@SerialVersionUID(1L)
class Node extends Serializable {
  var id : Int = -1
  var name : String = null
  var ip : String = null
  var port : Integer = null
  var longitude : Double = 0
  var latitude : Double = 0

  def this(name : String, ip : String, port : Integer) {
    this()
    this.id = id
    this.name = name
    this.ip = ip
    this.port = port
  }

  def this(name : String, ip : String, port : Integer, latitude : Double, longitude : Double) {
    this()
    this.id = id
    this.name = name
    this.ip = ip
    this.port = port
    this.longitude = longitude
    this.latitude = latitude
  }
}
