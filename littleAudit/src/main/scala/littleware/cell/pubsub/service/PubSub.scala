package littleware.cell.pubsub.service

import com.google.gson
import java.net.URI
import java.util.UUID
import littleware.cell.pubsub
import littleware.cloudutil

/**
 * Internal API for posting and polling for time-based messages to topics.
 * For example, this interface supports service provider responses to clients
 * where the backend service provider publishes messages to a topic that
 * a remote service client subscriber polls:
 * - client establishes a session to post requests to, and gets
 *       a topic for polling request status
 * - client posts a request to its session - backend launches an
 *       asynchronous job to process the request with the topic to
 *       post data back to
 * - async job posts status messages to the topic
 * - client polls the topic for status as needed
 */
trait PubSub {
    /**
     * Publish some events to a topic
     *
     * @param payloads should not exceed MAX_BATCH_SIZE
     * @return the ids of the posted events
     */
    def postEvents(
        cx:cloudutil.RequestContext, topic:String, payloads:Seq[gson.JsonObject]
    ):Seq[cloudutil.TimeId];


    /**
     * Poll a topic for new events.
     *
     * @param after position in publication stream
     */
    def pollForEvents(
        cx:cloudutil.RequestContext,
        topic: String,
        after: cloudutil.TimeId,
        limit: Int
    ): PubSub.QueryResult;
    
}

object PubSub {
    val MAX_BATCH_SIZE = 10

    case class Event(
        cx: cloudutil.RequestContext,
        id: cloudutil.TimeId,
        payload: Payload
        ) {}
    
    case class Payload(
        schema: URI,
        data: gson.JsonObject
    ) {}

    case class QueryResult(
        cursor: cloudutil.TimeId,
        data: Seq[Event],
        //
        // view of id's from the previous minute window,
        // so client can come back and retrieve missed
        // messages if necessary ...
        //
        rearView: Seq[cloudutil.TimeId]
    ) {}

}
