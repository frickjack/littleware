package littleware.cell.pubsub

import com.google.gson
import java.net.URI
import littleware.cloudutil


case class RequestEnvelope (
    cx: cloudutil.RequestContext,
    msgId: cloudutil.TimeId,
    msg: RequestEnvelope.Message
) {}


object RequestEnvelope {
    case class Message(
        actionPaths: Map[String, Seq[cloudutil.LRPath]],
        payloadSchema: URI,
        payload: gson.JsonObject
    ) {}
}