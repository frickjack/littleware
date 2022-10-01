package littleware.cell.pubsub.service

import com.google.gson
import java.net.URI
import java.util.UUID
import littleware.cell.pubsub
import littleware.cloudutil

/**
 * Interface accepts API request messages from a client consumer.
 * On the backend each client message is routed through a series
 * of event handlers, starting with authz, then proceeding to 
 * the service provider appropriate for the message.
 * An implementations of this interface accepts messages
 * from a client, and routes each message to the appropriate
 * event bus.
 * The various event handlers use the PubSub interface to publish
 * response messages for the client, and the client polls PubSub
 * for these responses.
 *
 * A client service consumer generally interacts with the system
 * via a REST API published to an API gateway.
 * A service provider interacts via a backend interaction that
 * is implementation dependent - could be Kafka topic,
 * EventBus dispatch, lambda invocation, ... whatever.
 */
trait Broker {
    /*
    A new API request starts a chain of asynchronous activity.
    The client polls the chain for status.
    Each chain maintains its own clock.
    The initial loop has t=0.
    A chain may branch - have multiple children - so that 
    different services may add to the chain,
    then join back up again.

    case class LoopId (chainId:UUID, timestamp:Int) {}
    
    startActivity():LoopId
    postLoop(loop:LoopId):LoopId
    postLoop(loop:LoopId, childLoops:Set[String]):Map[String,LoopId]
    */


    
    authorizeActivity(cx:cloudutil.RequestContext, id:UUID, isAuthorized:Boolean, message:String): UUID;

    /**
     * Return the set of listener activity ids that were signaled
     */
    completeActivity(cx:cloudutil.RequestContext, activityId: UUID, payload: Broker.Payload): Set[UUID]

    postMessage(cx:cloudutil.RequestContext, activityId: UUID, payload: Broker.Payload): UUID

    /**
     * We want some race-condition safe way to schedule an activity
     * after some other activities have completed ...
     * somehow leverage the DynamoTable transaction and TTL support.
     * Probably require an activity to be scheduled before the
     * activities it wants to listen to,
     * so build the tree from the bottom up ?
     */
    scheduleActivity(cx:cloudutil.RequestContext, payload: Broker.Payload):UUID

    fetchLastMessage(cx:cloudutil.RequestContext, activityId:UUID, lastReceived:UUID): Optional[Broker.Payload];

    listMessages(cx:cloudutil.RequestContext, activityId:UUID):Map[String, UUID];
    
    fetchResult(cx:cloudutil.RequestContext, activityId:UUID): Optional[Broker.Payload]

    getStatus(cx:cloudutil.RequestContext, activityId:UUID): Broker.Activity
}

object Broker {
    trait Payload {
        schema: URI
    }

    case class Activity(
        id: UUID,
        state: String
    ) {}

}
