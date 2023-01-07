package littleware.cell.pubsub

import com.google.gson
import java.util.UUID
import littleware.cloudutil

case class ResponseEnvelope (
    cx: cloudutil.RequestContext,
    msg: RequestEnvelope.Message
) {}


object ResponseEnvelope {
    object States {
        val processing = "processing";
        val success = "success";
        val failed = "failed";
    }

    case class Message (
        responseId: UUID,
        responseTimeMs: Long,
        state: String,
        progress: Int,
        payload: gson.JsonObject       
    ) {}
}