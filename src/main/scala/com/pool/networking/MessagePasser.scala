package com.pool.networking

import java.io._
import java.net._
import java.util
import java.util._
import android.util.Log

/**
 * Created by Lawrence on 4/15/15.
 */
class MessagePasser {
  var self : Node = null
  var bootstrapServer : String = null
  var bootstrapPort : Int = -1
  var config : Configuration = null
  var conns : HashMap[Integer, Socket] = null
  var oosMap : HashMap[Integer, ObjectOutputStream] = null
  var receiveQueue : util.LinkedList[Message] = null

  def this(localName : String, localPort : String, bootstrapServer : String, bootstrapPort : String) {
    this()

    this.bootstrapServer = bootstrapServer
    this.bootstrapPort = bootstrapPort.toInt
    conns = new util.HashMap[Integer, Socket]()
    oosMap = new util.HashMap[Integer, ObjectOutputStream]()
    receiveQueue = new util.LinkedList[Message]()

    Log.w("Pool", "Before bootstrap")
    bootstrap(localName, localPort)
    Log.w("Pool", "After bootstrap")

    //start connecting
    val nodeList = new util.ArrayList[Integer](config.nodes.keySet)
    Collections.sort(nodeList)
    var cf : ConnectionFormer = new ConnectionFormer(nodeList, this)
    new Thread(cf).start()

    //start listening
    var server : Server = new Server(this)
    new Thread(server).start()
  }

  /**
   * retrive configuration from bootstrap server
   * @param localName
   * @param localPort
   */
  def bootstrap(localName : String, localPort : String): Unit = {
    //create self node
    val tmpNode: Node = new Node(localName, null, localPort.toInt) //no way to know my public ip
    val sock: Socket = new Socket()

    Log.w("Pool", "connecting to bootstrap")

    //connect to bootstrap server
    try {
      sock.connect(new InetSocketAddress(bootstrapServer, bootstrapPort.toInt), 500)
    } catch {
      case e: Exception => println("Error connecting to bootstrap server: " + e)
    }
    Log.w("Pool", "sending info to bootstrap")

    //send my info
    try {
      val os = new ObjectOutputStream(sock.getOutputStream)
      os.writeObject(new BootstrapMessage(tmpNode, 0)) //0 for init
    } catch {
      case e: Exception => println("Error sending info to bootstrap server: " + e)
    }
    Log.w("Pool", "receiving info bootstrap")

    //receive bootstrap info
    try {
      val is: ObjectInputStream = new ObjectInputStream(sock.getInputStream);
      self = is.readObject().asInstanceOf[Node]
      config = is.readObject().asInstanceOf[Configuration]

      println("My id is:" + self.id)
    } catch {
      case e: Exception => println("Error receiving info from bootstrap server: " + e)
    }

    sock.close()
  }

  /**
   * inform bootstrap server before exiting
   */
  def exit(): Unit = {
    val sock: Socket = new Socket()

    //connect to bootstrap server
    try {
      sock.connect(new InetSocketAddress(bootstrapServer, bootstrapPort.toInt), 10)
    } catch {
      case e: Exception => println("Error connecting to bootstrap server: " + e)
    }

    //send my info
    try {
      val os = new ObjectOutputStream(sock.getOutputStream)
      os.writeObject(new BootstrapMessage(self, 1)) //1 for exit
    } catch {
      case e: Exception => println("Error sending info to bootstrap server: " + e)
    }

    sock.close()
  }

  def send(msg : Message, dest : Int): Unit = {
    if (conns.get(dest) == null) {
      println("Destination does not exist: " + dest)
      return;
    }
    msg.src = this.self.id
    deliver(msg, dest)
  }

  def broadcast(msg : Message): Unit = {
    val keys = conns.keySet()
    val it = keys.iterator()
    while(it.hasNext) {
      send(msg, it.next())
    }
  }

  def deliver(msg : Message, dest : Int): Unit = {
    val oos : ObjectOutputStream = oosMap.get(dest)
    try {
      oos.writeObject(msg)
      oos.flush()
      oos.reset()
    } catch {
      case e: IOException => println("Unable to deliver message to " + dest)
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
                println("Connecting to: " + destNode.ip + " " + destNode.port)

                val s: Socket = new Socket()
                s.connect(new InetSocketAddress(destNode.ip, destNode.port), 500)
                conns.put(nodeId, s)
                oosMap.put(nodeId, new ObjectOutputStream(s.getOutputStream()))
                oosMap.get(nodeId).writeObject(self)

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
          case e: InterruptedIOException => println("Connection establisher interrupted");
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
      println("Connection established with " + remoteNodeId)
      while (connectionOpen) try {
        msg = ois.readObject().asInstanceOf[Message]
        println(msg.text)
        receiveQueue.add(msg)
      } catch {
        case e: ClassNotFoundException => ;
        case e: IOException => handleIOException(remoteNodeId)
      }
    }

    def handleIOException(remoteNodeId : Int): Unit = {
      println("Connection with " + remoteNodeId + " has been closed")
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
        case e: IOException => ;
      }

      while (true) {
        try {
          println("Waiting for connection ")

          val newConn : Socket = serverSocket.accept()
          val ois : ObjectInputStream = new ObjectInputStream(newConn.getInputStream);
          val remoteNode : Node = ois.readObject().asInstanceOf[Node]

          config.nodes.put(remoteNode.id, remoteNode) //add new node
          conns.put(remoteNode.id, newConn)
          oosMap.put(remoteNode.id, new ObjectOutputStream(newConn.getOutputStream()))

          var listener : Listener = new Listener(mp, ois, remoteNode.id)
          new Thread(listener).start()
        } catch {
          case e: IOException => ;
        }
      }
    }
  }
}
