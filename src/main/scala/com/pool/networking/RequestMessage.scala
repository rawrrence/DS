package com.pool.networking

@SerialVersionUID(1L)
class RequestMessage extends Serializable {
    var requesterName : String = "Anonymous"
    var requestNode : Node = null
    var startTime : Long = -1
    var endTime : Long = -1
    var note : String = null
    var srcNode : Node = null


    def this(srcNode: Node, requesterName : String, requestNode : Node, startTime : Long, endTime : Long, note : String) {
        this()
        this.srcNode = srcNode
        this.requesterName = requesterName
        this.requestNode = requestNode
        this.startTime = startTime
        this.endTime = endTime
        this.note = note
    }
}
