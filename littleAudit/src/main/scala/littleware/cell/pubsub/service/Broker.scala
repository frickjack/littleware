package littleware.cell.pubsub.service

import com.google.gson
import java.util.UUID
import littleware.cell.pubsub
import littleware.cloudutil

/**
 * Interface accepts API request messages from a service consumer,
 * allows a service provider to post a response to a message,
 * and allows a service consumer to poll for a response.
 * A service consumer generally interacts with the system
 * via a REST API published to an API gateway.
 * A service provider interacts via a backend interaction that
 * is implementation dependent - could be Kafka topic,
 * EventBus dispatch, lambda invocation, ... whatever.
 */
trait Broker {
    /**
     * For service consumer to post a request message.
     * Note that the order that messages are retrieved in
     * is random - we cannot assume the second message is
     * processed after the first message
     *
     * @param bus name of bus whose lifetime spans across sessions
     * @return the request id
     */
    def postMessages(
        cx:cloudutil.RequestContext, bus:String, messages:Seq[pubsub.RequestEnvelope.Message]
    ):Seq[UUID];

    /**
     * For service consumer to poll for a response to a previous request
     */
    def pollForResponses(
        cx:cloudutil.RequestContext,
        bus:String,
        after:cloudutil.TimeId
    ): Seq[pubsub.ResponseEnvelope] = Nil
    
    /**
     * For service providers to post a response
     *
     * @return response id
     */
    def postResponse(
        cx: cloudutil.RequestContext,
        bus: String,
        state: String,
        progress: Int,
        payload: gson.JsonObject
    ): UUID = null    
    
}
