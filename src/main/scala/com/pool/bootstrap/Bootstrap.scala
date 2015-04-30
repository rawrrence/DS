package com.pool.bootstrap


import java.io._
import java.net._
import java.util

import com.pool.networking._

/**
 * Created by Siqi on 4/19/15.
 */
class Bootstrap {
  var config : Configuration = null
  var port : Int = -1
  var idCounter : Int = 0 //for generating unique id

  def this(bootstrapPort : String) {
    this()

    port = bootstrapPort.toInt
    config = new Configuration
    config.nodes = new util.HashMap[Integer, Node]

    //start listening
    var server : Server = new Server()
    new Thread(server).start()
  }


  class Server extends Runnable {
    override def run(): Unit = {
      var serverSocket : ServerSocket = null

      try {
        serverSocket = new ServerSocket(port)
      } catch {
        case e: IOException => ;
      }

      while (true) {
        try {
          val newConn : Socket = serverSocket.accept()
          val is : ObjectInputStream = new ObjectInputStream(newConn.getInputStream)
          val os : ObjectOutputStream = new ObjectOutputStream(newConn.getOutputStream)

          println("Connection established")

          val msg : BootstrapMessage = is.readObject().asInstanceOf[BootstrapMessage]
          val remoteNode = msg.node

          println("Package flag: " + msg.flag)

          if (msg.flag == 0) {
            //init
            //set id
            remoteNode.id = idCounter
            idCounter += 1

            //ip and port
            var addr : InetSocketAddress = newConn.getRemoteSocketAddress.asInstanceOf[InetSocketAddress]
            remoteNode.ip = addr.getAddress.getHostAddress;

            //send back info
            os.writeObject(remoteNode)
            os.writeObject(config)

            //add to config
            config.nodes.put(remoteNode.id, remoteNode) //add new node
            println("Add " + remoteNode.id + " to bootstrap server")
            println("Ip " + remoteNode.ip)
            println("Port " + remoteNode.port)
            println( "Location: " + remoteNode.latitude.toString + " " + remoteNode.longitude.toString)


          } else if (msg.flag == 1) {
            config.nodes.remove(remoteNode.id)
            println("Remove " + remoteNode.id + " from bootstrap server")

          } else if (msg.flag == 2) {
            config.nodes.get(remoteNode.id).latitude = msg.node.latitude
            config.nodes.get(remoteNode.id).longitude = msg.node.longitude
            println("Update " + remoteNode.id)
            println( "Location: " + remoteNode.latitude.toString + " " + remoteNode.longitude.toString)

          } else {
            println("BootstrapMessage: wrong flag")
          }

        } catch {
          case e: IOException => println("Bootstrap: " + e)
        }
      }
    }
  }
}
