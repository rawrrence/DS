package com.pool.networking

import java.io._
import java.net._
import java.security.Security
import java.util
import java.util._

import android.location.Location
import android.util.Log
import java.util.Collections

/**
 * Created by Lawrence on 4/15/15.
 */
class MessagePasser {
  val Request : String = "REQUEST"
  val Reply : String = "REPLY"
  val Cancel : String = "CANCEL"

  var currentSeqNum = 0
  var self : Node = null
  //var bootstrapServer : String = null
  var serverList : util.ArrayList[String] = null
  var curServerIndex = 0
  var bootstrapPort : Int = -1
  var config : Configuration = null

  var bootstrapDone : Boolean = false

  var conns : HashMap[Integer, Socket] = null
  var oosMap : HashMap[Integer, ObjectOutputStream] = null
  var locationService : LocationService = null

  val TIMEOUT = 3000 //3 sec
  val MAX_DISTANCE = 100000 //100 km

  var receivedRequests : util.LinkedList[Message] = null
  var receivedReplies : util.LinkedList[Message] = null
  var sentRequests : util.ArrayList[Message] = null


  def this(locationService : LocationService, localName : String, localPort : String, serverList : util.ArrayList[String], bootstrapPort : String) {
    this()
    System.setProperty("networkaddress.cache.ttl", "0")
    Security.setProperty("networkaddress.cache.ttl", "0")
    Log.w("Pool", "System dns cache ttl: " + System.getProperty("networkaddress.cache.ttl"))
    Log.w("Pool", "JAVA dns cache ttl: " + Security.getProperty("networkaddress.cache.ttl"))

    this.locationService = locationService
    locationService.mp = this
    //this.bootstrapServer = bootstrapServer
    this.serverList = serverList
    this.curServerIndex = (Math.random() * serverList.size).asInstanceOf[Int]
    this.bootstrapPort = bootstrapPort.toInt
    conns = new util.HashMap[Integer, Socket]()
    oosMap = new util.HashMap[Integer, ObjectOutputStream]()

    Log.w("Pool", "Before bootstrap")
    var bootstrap : Bootstrapper = new Bootstrapper(localName, localPort)
    var thr : Thread = new Thread(bootstrap)
    thr.start
    thr.join
    //while (bootstrapDone == false) {}
    Log.w("Pool", "After bootstrap")

    //start sending heartbeat messages
    var hb : HeartbeatSender = new HeartbeatSender(this)
    new Thread(hb).start()

    receivedRequests = new util.LinkedList[Message]()
    receivedReplies = new util.LinkedList[Message]()
    sentRequests = new util.ArrayList[Message]()

    //start connecting
    val nodeList = new util.ArrayList[Integer](config.nodes.keySet)
    Collections.sort(nodeList)

    var cf : ConnectionFormer = new ConnectionFormer(nodeList, this)
    new Thread(cf).start()

    //start listening
    var server : Server = new Server(this)
    new Thread(server).start()
  }

  class Bootstrapper extends Runnable {

    var localName : String = null
    var localPort : String = null

    def this(localName : String, localPort : String) {
      this()
      this.localName = localName
      this.localPort = localPort
    }

    override def run(): Unit = {
      var loc: Location = locationService.getLocation()
      while (loc == null) {
        loc = locationService.getLocation()
      }
      Log.w("Pool", "Bootstraping:")
      Log.w("Pool", "Initial location: " + loc.getLatitude() + " " + loc.getLongitude())

      //create self node
      val tmpNode: Node = new Node(localName, null, localPort.toInt, loc.getLatitude(), loc.getLongitude()) //no way to know my public ip
      var pass = true
      do {
        var sock : Socket = null
        pass = true
        try {
          sock = new Socket()

          Log.w("Pool", "connecting to bootsrap")
          //connect to bootstrap server
          sock.connect(new InetSocketAddress(serverList.get(curServerIndex), bootstrapPort.toInt), TIMEOUT)


          Log.w("Pool", "sending info to bootsrap")
          //send my info
          val os = new ObjectOutputStream(sock.getOutputStream)
          os.writeObject(new BootstrapMessage(tmpNode, 0)) //0 for init
          os.flush()

          Log.w("Pool", "receiving info from bootsrap")
          //receive bootstrap info
          val is: ObjectInputStream = new ObjectInputStream(sock.getInputStream)
          self = is.readObject().asInstanceOf[Node]
          config = is.readObject().asInstanceOf[Configuration]

          Log.w("Pool", "My id is:" + self.id)
          Thread.sleep(1000)
          sock.close()
        } catch {
          case e: Exception => {
            Log.w("Pool", "Error bootstrapping, retrying: " + e)
            pass = false
            if (sock != null)
              Thread.sleep(1000)
              sock.close()
            curServerIndex = (curServerIndex + 1)%serverList.size
            Thread.sleep(1000)

          }
        }
      } while (!pass)


      /*
      //filter nodes using location
      val itr = config.nodes.keySet().iterator()
      while (itr.hasNext()) {
        val key = itr.next
        val node = config.nodes.get(key)
        var result = new Array[Float](1)
        Location.distanceBetween(node.latitude, node.longitude, self.latitude, self.longitude, result)

        Log.w("Pool", "Distance with " + key + " is " + result(0))
        if (result(0) > MAX_DISTANCE) {
          itr.remove()
        }
      }
      */
      bootstrapDone = true
    }
  }

  class LocationUpdater extends Runnable {

    var localName : String = null
    var localPort : String = null

    def this(localName : String, localPort : String) {
      this()
      this.localName = localName
      this.localPort = localPort
    }

    override def run(): Unit = {
      if (!bootstrapDone) return

      var loc : Location = locationService.getLocation()
      Log.w("Pool", "Updating:")
      Log.w("Pool", "Update location: " + loc.getLatitude() + " " + loc.getLongitude())

      //update self node
      self.latitude = loc.getLatitude
      self.longitude = loc.getLongitude

      var sock: Socket = null
      try {
        sock = new Socket()

        Log.w("Pool", "connecting to bootsrap")
        //connect to bootstrap server

        sock.connect(new InetSocketAddress(serverList.get(curServerIndex), bootstrapPort.toInt), TIMEOUT)


        Log.w("Pool", "sending info to bootsrap")
        //send my info

        val os = new ObjectOutputStream(sock.getOutputStream)
        os.writeObject(new BootstrapMessage(self, 2)) //0 for init
        os.flush()
        Thread.sleep(1000)
        sock.close()
      } catch {
        case e: Exception =>{
          Log.w("Pool", "Error updating location: " + e)
          if (sock != null) {
            Thread.sleep(1000)
            sock.close()
          }
          curServerIndex = (curServerIndex + 1)%serverList.size
        }
      }


    }
  }

  class ExitUpdater extends Runnable {

    var localName : String = null
    var localPort : String = null

    def this(localName : String, localPort : String) {
      this()
      this.localName = localName
      this.localPort = localPort
    }

    override def run(): Unit = {
      if (!bootstrapDone) return

      var sock: Socket = null

      Log.w("Pool", "Exiting !")
      //connect to bootstrap server
      try {
        sock = new Socket()
        sock.connect(new InetSocketAddress(serverList.get(curServerIndex), bootstrapPort.toInt), TIMEOUT)

        //send my info
        val os = new ObjectOutputStream(sock.getOutputStream)
        val msg : BootstrapMessage = new BootstrapMessage(self, 1)
        os.writeObject(msg) //1 for exit
        os.flush()
        Thread.sleep(1000)
        sock.close()
      } catch {
        case e: Exception =>{
          Log.w("Pool", "Error exiting: " + e)
          if (sock != null) {
            Thread.sleep(1000)
            sock.close()
          }
          curServerIndex = (curServerIndex + 1)%serverList.size
        }
      }


    }
  }

  class HeartbeatSender extends Runnable {
    var mp : MessagePasser = null;

    val maxAttempts = 2;

    def this(mp : MessagePasser) {
      this()
      this.mp = mp
    }

    override def run(): Unit = {
      var heartOpen = false
      var sock : Socket = null
      while (true) {
        try {
          if (!heartOpen) {
            curServerIndex = (curServerIndex + 1) % serverList.size()
            sock = new Socket()
            sock.connect(new InetSocketAddress(serverList.get(curServerIndex), bootstrapPort.toInt), TIMEOUT)
            heartOpen = true
          }
          Log.w("Pool", "Attempting to send a heartbeat message to bootstrap")

          val os = new ObjectOutputStream(sock.getOutputStream)
          os.writeObject(new BootstrapMessage(self, 3)) // 3 for heartbeat
          os.flush()
          Thread.sleep(1000)
        } catch {
          case e: Exception => {
            Log.w("Pool", "Sending heartbeat unsuccessful, retrying: " + e)
            Thread.sleep(1000)
            heartOpen = false
            sock.close()
          }
        }
      }
    }
  }


  /**
   * inform bootstrap server of location change
   */
  def updateLocation(): Unit = {
    var updater : LocationUpdater = new LocationUpdater(self.name, self.port.toString)
    var thr : Thread = new Thread(updater)
    thr.start
    thr.join
  }

  /**
   * inform bootstrap server before exiting
   */
  def exit(): Unit = {
    var updater : ExitUpdater = new ExitUpdater(self.name, self.port.toString)
    var thr : Thread = new Thread(updater)
    thr.start
    thr.join
  }

  def send(msg : Message, dest : Int): Unit = {
    if (conns.get(dest) == null) {
      Log.w("Pool", "Destination does not exist: " + dest)
      return
    }
    msg.src = this.self.id
    deliver(msg, dest)
  }

  def broadcast(msg : Message): Unit = {
    msg.seqNum = currentSeqNum
    currentSeqNum = currentSeqNum + 1
    sentRequests.add(msg)
    val keys = conns.keySet()
    val it = keys.iterator()
    while(it.hasNext) {
      send(msg, it.next())
    }
  }

  def deliver(msg : Message, dest : Int): Unit = {
    val oos : ObjectOutputStream = oosMap.get(dest)
    var result = new Array[Float](1)
    var node : Node = config.nodes.get(dest)
    Location.distanceBetween(node.latitude, node.longitude, self.latitude, self.longitude, result)
    Log.w("Pool", "Distance with " + dest + " is " + result(0))
    if (result(0) > MAX_DISTANCE) {
      Log.w("Pool", "Not delivering because distance too far")
      return
    }
    try {
      oos.writeObject(msg)
      oos.flush()
      oos.reset()
    } catch {
      case e: IOException => Log.w("Pool", "Unable to deliver message to " + dest)
    }
  }

  class ConnectionFormer extends Runnable {
    var nodeList : util.ArrayList[Integer] = null
    var mp : MessagePasser = null

    def this(nodeList : util.ArrayList[Integer], mp : MessagePasser) {
      this()
      this.nodeList = nodeList
      this.mp = mp
    }

    override def run() : Unit = {
      while (true) {
        var passed = false
        val it = nodeList.iterator()

        while (it.hasNext()) {
          val nodeId : Int = it.next()
          if  (nodeId >= self.id) {
            passed = true
          }

          if (!passed) {
            if (!conns.keySet().contains(nodeId)) {
              val destNode: Node = config.nodes.get(nodeId)
              try {
                Log.w("Pool", "Connecting to: " + destNode.ip + " " + destNode.port)

                val s: Socket = new Socket()
                s.connect(new InetSocketAddress(destNode.ip, destNode.port), TIMEOUT)
                conns.put(nodeId, s)
                oosMap.put(nodeId, new ObjectOutputStream(s.getOutputStream()))
                oosMap.get(nodeId).writeObject(self)
                oosMap.get(nodeId).flush()

                var listener : Listener = new Listener(mp, new ObjectInputStream(s.getInputStream()), nodeId)
                new Thread(listener).start()
              } catch {
                case e: UnknownHostException => ;
                case e: IOException => ;
              }
            }
          }
        }
        try {
          Thread.sleep(10000);
        } catch {
          case e: InterruptedIOException => Log.w("Pool", "Connection establisher interrupted");
        }
      }
    }
  }
  class Listener extends Runnable {
    var mp : MessagePasser = null
    var ois : ObjectInputStream = null
    var remoteNodeId : Int = -1
    var connectionOpen : Boolean = true

    def this(mp : MessagePasser, ois : ObjectInputStream, remoteNodeId : Int) {
      this()
      this.mp = mp
      this.ois = ois
      this.remoteNodeId = remoteNodeId
      connectionOpen = true
    }

    override def run() : Unit = {
      var msg: Message = null
      Log.w("Pool", "Connection established with " + remoteNodeId)
      while (connectionOpen) try {
        var close = true
        msg = ois.readObject().asInstanceOf[Message]
        if (msg.mType.equals(Request)) {
          var node : Node = config.nodes.get(msg.src)
          var result = new Array[Float](1)
          Location.distanceBetween(node.latitude, node.longitude, self.latitude, self.longitude, result)
          Log.w("Pool", "Distance with " + msg.src + " is " + result(0))
          if (result(0) > MAX_DISTANCE) {
            Log.w("Pool", "Not delivering because distance too far")
            close = false
          }
          if (close) {
            receivedRequests.add(msg);
          }
        }
        if (msg.mType.equals(Reply)) {
          receivedReplies.add(msg)
        }
        if (msg.mType.equals(Cancel)) {
          if (msg.body == self.id.toString){
            receivedReplies.add(msg)
          }
            findAndRemoveRequest(msg)
        }
      } catch {
        case e: ClassNotFoundException => ;
        case e: IOException => handleIOException(remoteNodeId)
      }
    }

    def handleIOException(remoteNodeId : Int): Unit = {
      Log.w("Pool","Connection with " + remoteNodeId + " has been closed")
      conns.remove(remoteNodeId)
      oosMap.remove(remoteNodeId)
      connectionOpen = false
    }
  }

  class Server extends Runnable {
    var mp : MessagePasser = null

    def this(mp : MessagePasser) {
      this()
      this.mp = mp
    }

    override def run(): Unit = {
      var serverSocket : ServerSocket = null

      try {
        serverSocket = new ServerSocket(self.port)
      } catch {
        case e: IOException => Log.w("Pool", "Error creating listening port " + e);
      }

      while (true) {
        try {
          Log.w("Pool", "Waiting for connection ")

          val newConn : Socket = serverSocket.accept()
          val ois : ObjectInputStream = new ObjectInputStream(newConn.getInputStream);
          val remoteNode : Node = ois.readObject().asInstanceOf[Node]

          config.nodes.put(remoteNode.id, remoteNode) //add new node
          conns.put(remoteNode.id, newConn)
          oosMap.put(remoteNode.id, new ObjectOutputStream(newConn.getOutputStream()))

          val listener : Listener = new Listener(mp, ois, remoteNode.id)
          new Thread(listener).start()
        } catch {
          case e: IOException => ;
        }
      }
    }
  }


  def findAndRemoveRequest(msg : Message): Unit = {
    val it = receivedRequests.iterator()
    while(it.hasNext) {
      val m = it.next()
      if (m.src == msg.src && m.seqNum == msg.seqNum) {
        receivedRequests.remove(m)

      }
    }
  }

  def acceptReply(msg : Message): Unit = {
    val it = sentRequests.iterator()
    while(it.hasNext) {
      val m = it.next()
      if (m.seqNum == msg.seqNum) {
        sentRequests.remove(m)
        m.body = msg.src.toString
        m.setMessageType("CANCEL")
        broadcast(m)
      }
    }
  }

}
