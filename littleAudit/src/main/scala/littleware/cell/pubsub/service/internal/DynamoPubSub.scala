package littleware.cell.pubsub.service.internal

import collection.JavaConverters._

import com.google.gson
import com.google.inject
import java.util.Date
import java.util.UUID
import littleware.cell.pubsub
import littleware.cell.pubsub.service.PubSub
import littleware.cloudutil
import software.amazon.awssdk.services.dynamodb


/**
 * PubSub implementation that
 * tracks message queues in Dynamodb
 *
 * @see https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-items.html
 * @see https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-eventbridge.html
 * @see https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/dynamodb/package-summary.html
 * @see https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html
 * @see https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/time-to-live-ttl-before-you-start.html#time-to-live-ttl-before-you-start-formatting
 */
class DynamoPubSub @inject.Inject() (dynamo:dynamodb.DynamoDbClient, gs:gson.Gson, config:DynamoPubSub.Config) extends PubSub {

    private val openWindowCache = new java.util.concurrent.ConcurrentHashMap[DynamoPubSub.Topic, Long](10000).asScala

    private def saveEvents(eventSeq:Seq[DynamoPubSub.Envelope]):Unit = {
        if (eventSeq.isEmpty) {
            return
        }
        if (eventSeq.length > pubsub.service.PubSub.MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("max batch size is: " + pubsub.service.PubSub.MAX_BATCH_SIZE)
        }
        val tableName = config.tableDomain + "/" + DynamoPubSub.TABLE_NAME
        val batch = dynamodb.model.BatchWriteItemRequest.builder().requestItems(
            Seq(
                tableName -> eventSeq.map(
                    {
                        ev => 
                        val attrMap:Map[String, dynamodb.model.AttributeValue] = Map(
                            "TopicKey" -> ev.topicKey.toString(),
                            "TimeId" -> ev.timeId.toString(),
                            "Context" -> gs.toJson(ev.cx),
                            "Payload" -> ev.payload.toString()
                        ).map(
                            kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                        )
                        dynamodb.model.WriteRequest.builder().putRequest(
                            dynamodb.model.PutRequest.builder().item(
                                attrMap.asJava
                            ).build()
                        ).build()
                    }
                ).asJava
            ).toMap.asJava
        ).build()
        var resp = dynamo.batchWriteItem(batch)
        while (resp.hasUnprocessedItems()) {
            resp = dynamo.batchWriteItem(
                dynamodb.model.BatchWriteItemRequest.builder().requestItems(
                    resp.unprocessedItems()
                ).build()
            )
        }
    }

    /**
     * Save a window entry in the db if necessary
     * Ensure that all the generated events have ids that fall within the window
     */
    private def openWindow(cx:cloudutil.RequestContext, topic:String, payloads:Seq[gson.JsonObject]):Seq[DynamoPubSub.Envelope] = {
        val nowMs = new java.util.Date().getTime()
        val window = Math.floorDiv(nowMs, DynamoPubSub.WINDOW_SIZE_MS)
        val events = payloads.map(
            {
                pl =>
                 new DynamoPubSub.Envelope(
                     DynamoPubSub.TopicWindow(cx.session.projectId, topic, window),
                     cloudutil.TimeId(UUID.randomUUID(), nowMs),
                     cx, pl
                 )
            }
        )

        val cacheKey = DynamoPubSub.Topic(cx.session.projectId, topic)
        val cacheValue = openWindowCache.getOrElse(cacheKey, 0L)

        if (cacheValue != window) {
            // better save the window to the database
            val windowTopic = "window/" + topic
            val day = Math.floorDiv(window, DynamoPubSub.WINDOW_FRAME_SIZE)
            val windowEvent = new DynamoPubSub.Envelope(
                DynamoPubSub.TopicWindow(cx.session.projectId, windowTopic, day),
                cloudutil.TimeId(cloudutil.TimeId.zeroId, window * DynamoPubSub.WINDOW_SIZE_MS),
                cx, new gson.JsonObject()
            )
            saveEvents(Seq(windowEvent))
            // if the same window is still current, then cache it
            if (window == Math.floorDiv(new Date().getTime(), DynamoPubSub.WINDOW_SIZE_MS)) {
                openWindowCache.put(cacheKey, window)
            }
        }
        return events
    }

    def postEvents(
        cx:cloudutil.RequestContext, topic:String, payloads:Seq[gson.JsonObject]
    ):Seq[cloudutil.TimeId] = {
        if (! DynamoPubSub.isTopicValid(topic)) {
            throw new IllegalArgumentException("invalid topic: " + topic)
        }
        if (payloads.length < 1) {
            return Seq.empty
        }
        if (payloads.length > 20) {
            throw new IllegalArgumentException("batch may not exceed 20 entries, got: " + payloads.length)
        }
        val events = openWindow(cx, topic, payloads)
        return Seq()
    }

    /**
     * Poll a topic for new events
     */
    def pollForEvents(
        cx:cloudutil.RequestContext,
        topic: String,
        afterThis: cloudutil.TimeId
    ): Seq[PubSub.Event] = Seq()
    
    def pollForEvents(
        cx:cloudutil.RequestContext,
        topic: String,
        afterThis: java.time.LocalTime
    ): Seq[PubSub.Event] = Seq()

}


object DynamoPubSub {
    val MIN_READ_DELAY_MS = 1000
    val WINDOW_SIZE_MS = 60000
    // how many windows do we want to track 
    // at the 2nd "frame" level of the tree
    val WINDOW_FRAME_SIZE = 1440
    val TABLE_NAME = "littlePubSub"

    case class Config(
        tableDomain:String
    ) {}

    case class Topic(
        projectId: UUID,
        topic: String
    ) {}

    case class TopicWindow (
        projectId: UUID,
        topic: String,
        timeWindow: Long
    ) {
        override def toString():String = 
            "" + projectId + ":" + ":" + topic + ":" + cloudutil.TimeId.pad20(timeWindow, 9)
    }


    case class Envelope (
        topicKey: TopicWindow,
        timeId: cloudutil.TimeId,
        cx: cloudutil.RequestContext,
        payload: gson.JsonObject
    ) {}

    val topicRx = raw"[\w-]+".r
    def isTopicValid(topic:String) = null != topic && topicRx.matches(topic)

    @inject.Singleton
    class Provider() extends inject.Provider[DynamoPubSub] {
        override def get():DynamoPubSub = { throw new UnsupportedOperationException("not yet implemented") }
    }
}
