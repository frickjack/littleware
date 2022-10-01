package littleware.cell.pubsub

import com.google.gson
import java.net.URI
import java.util.UUID
import littleware.cloudutil

case class ResponseEnvelope (
    cx: cloudutil.RequestContext,
    responseId: cloudutil.TimeId,
    response: ResponseEnvelope.Message,
    // Id of a message this is a response to
    msgId: cloudutil.TimeId
) {}


object ResponseEnvelope {
    object States {
        val processing = "processing";
        val success = "success";
        val failed = "failed";
    }

    case class Message (
        state: String,
        progress: Int,
        payloadSchema: URI,
        payload: gson.JsonObject       
    ) {}
}