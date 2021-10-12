package littleware.cell.pubsub.service

import com.google.gson
import java.util.UUID
import littleware.cell.pubsub
import littleware.cloudutil

/**
 * Internal API for posting and polling for time-based messages to topics.
 */
trait PubSub {
    /**
     * Publish some events to a topic
     *
     * @return the ids of the posted events
     */
    def postEvents(
        cx:cloudutil.RequestContext, topic:String, payloads:Seq[gson.JsonObject]
    ):Seq[cloudutil.TimeId];

    /**
     * Poll a topic for new events
     */
    def pollForEvents(
        cx:cloudutil.RequestContext,
        topic: String,
        afterThis: cloudutil.TimeId
    ): Seq[PubSub.Event];
    
    def pollForEvents(
        cx:cloudutil.RequestContext,
        topic: String,
        afterThis: java.time.LocalTime
    ): Seq[PubSub.Event];

}

object PubSub {
    val MAX_BATCH_SIZE = 10
    case class Event(cx:cloudutil.RequestContext, payload:gson.JsonObject) {}
}