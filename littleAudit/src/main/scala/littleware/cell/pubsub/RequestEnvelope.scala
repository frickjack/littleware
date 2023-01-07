package littleware.cell.pubsub

import littleware.cloudutil

import com.google.gson

case class RequestEnvelope (
    cx: cloudutil.RequestContext,
    msg: RequestEnvelope.Message
) {}


object RequestEnvelope {
    case class Message(
        actionPaths: Map[String, Seq[cloudutil.LRPath]],
        payload: gson.JsonObject
    ) {}
}