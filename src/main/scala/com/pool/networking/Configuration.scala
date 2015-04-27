package com.pool.networking

import java.util


/**
 * Created by Lawrence on 4/15/15.
 */
@SerialVersionUID(1000L)
class Configuration extends Serializable{

  var nodes : util.HashMap[Integer, Node] = null
}
