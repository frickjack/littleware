package littleware.cell.pubsub.service.internal

import com.google.gson
import com.google.{common => guava}
import com.google.inject
import java.util.Date
import java.util.UUID
import littleware.cell.pubsub
import littleware.cell.pubsub.service.PubSub
import littleware.cloudutil
import software.amazon.awssdk.services.dynamodb

import scala.jdk.CollectionConverters._


/**
 * PubSub implementation that
 * tracks message queues in Dynamodb
 *
 * @see https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-items.html
 * @see https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-eventbridge.html
 * @see https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/dynamodb/package-summary.html
 * @see https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html
 * @see https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/time-to-live-ttl-before-you-start.html#time-to-live-ttl-before-you-start-formatting
 *
 * This implementation attempts best effort at-least once delivery with client support.
 * Each time-based query includes not only the requested events,
 * but also the ids of up to 100 events in the topic preceding the first event,
 * so that a client polling the topic for the latest events can check whether
 * earlier events were skipped due to the eventual consistency inherent in the
 * distributed system.
 */
@inject.Singleton
class DynamoPubSub @inject.Inject() (dynamo:dynamodb.DynamoDbClient, gs:gson.Gson, config:DynamoPubSub.Config) extends PubSub {
    val tableName = config.tableDomain

    private val openWindowCache:guava.cache.Cache[DynamoPubSub.Topic, Long] = //new java.util.concurrent.ConcurrentHashMap[DynamoPubSub.Topic, Long](10000).asScala
        guava.cache.CacheBuilder.newBuilder()
       .maximumSize(10000)
       .expireAfterWrite(2, java.util.concurrent.TimeUnit.MINUTES)
       .build()

    private def saveEvents(eventSeq:Seq[DynamoPubSub.Envelope]):Unit = {
        if (eventSeq.isEmpty) {
            return
        }
        if (eventSeq.length > pubsub.service.PubSub.MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("max batch size is: " + pubsub.service.PubSub.MAX_BATCH_SIZE)
        }
        // Expire records out of dynamo after 1 hour
        val expireUnixTime = (new Date().getTime() / 1000).asInstanceOf[Int] + 36000
        val batch = dynamodb.model.BatchWriteItemRequest.builder().requestItems(
            Seq(
                tableName -> eventSeq.map(
                    {
                        ev => 
                        val attrMap:Map[String, dynamodb.model.AttributeValue] = Map(
                            "PK" -> ev.topicKey.toString(),
                            "SK" -> ev.timeId.toString(),
                            "Context" -> gs.toJson(ev.cx),
                            "Payload" -> gs.toJson(ev.payload)
                        ).map(
                            kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                        ) ++ Map(
                            "Expiration" -> dynamodb.model.AttributeValue.builder().n(expireUnixTime.toString()).build()
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
        DynamoPubSub.windowEnvelope(cx, topic, nowMs) match {
            case pk -> winEnvelope => {
                val events = payloads.map(
                    {
                        pl =>
                        new DynamoPubSub.Envelope(
                            pk,
                            cloudutil.TimeId(UUID.randomUUID(), nowMs),
                            cx, pl
                        )
                    }
                )

                val cacheKey = DynamoPubSub.Topic(cx.session.projectId, topic)
                val cacheValue = openWindowCache.get(cacheKey, () => 0L)

                if (cacheValue != pk.timeWindow) {
                    // better save the window to the database
                    saveEvents(Seq(winEnvelope))
                    // if the same window is still current, then cache it
                    if (pk.timeWindow == Math.floorDiv(new Date().getTime(), DynamoPubSub.WINDOW_SIZE_MS)) {
                        openWindowCache.put(cacheKey, pk.timeWindow)
                    }
                }
                events
            }
        }
    }

    override def postEvents(
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
        saveEvents(events)
        return events.map(it => it.timeId)
    }


    private def loadEvents(
        topicKey: DynamoPubSub.TopicWindow,
        after: cloudutil.TimeId,
        before: cloudutil.TimeId,
        withPayload: Boolean,
        limit: Int
    ): Seq[DynamoPubSub.TimeAndEvent] = {
        val query = dynamodb.model.QueryRequest.builder(
        ).tableName(tableName   
        ).attributesToGet(
            (Seq("SK") ++ (if (withPayload) { Seq("Payload", "Context") } else { Seq() })).asJava
        ).keyConditionExpression(
                    "PK = :TopicKey AND SK > :MinTimeId AND SK < :MaxTimeId"
        ).expressionAttributeValues(
            Map(
                "TopicKey" -> topicKey.toString(),
                "MinTimeId" -> after.toString(),
                "MaxTimeId" -> before.toString()
            ).map(
                kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
            ).asJava
        ).limit(
            (
                if (limit < 1) {
                    1
                } else if (withPayload && limit > PubSub.MAX_BATCH_SIZE) {
                    PubSub.MAX_BATCH_SIZE
                } else {
                    limit
                }
            )
        ).build()

        dynamo.query(query).items().asScala.toSeq.map(
            {
                attrMap => {
                    val timeId = cloudutil.TimeId.fromString(attrMap.get("TimeId").s())
                    DynamoPubSub.TimeAndEvent(
                        timeId,
                        Option.when(withPayload){
                                PubSub.Event(
                                    gs.fromJson(attrMap.get("Context").s(), classOf[cloudutil.RequestContext]),
                                    timeId,
                                    gs.fromJson(attrMap.get("Payload").s(), classOf[PubSub.Payload])
                                )
                        }
                    )
                }
            }
        )
    }
    
    
    override def pollForEvents(
        cx: cloudutil.RequestContext,
        topic: String,
        after: cloudutil.TimeId,
        limitIn: Int
    ): PubSub.QueryResult = {
        val nowId = cloudutil.TimeId(new Date().getTime())
        val limit = (
            if (limitIn < 0) { 1
            } else if (limitIn > PubSub.MAX_BATCH_SIZE) { PubSub.MAX_BATCH_SIZE
            } else { limitIn }
        )

        DynamoPubSub.windowEnvelope(cx, topic, after.timestamp) match {
            case _ -> winEnvelope => {
                // load up to 5 windows
                val windowIds = loadEvents(
                        winEnvelope.topicKey,
                        cloudutil.TimeId(winEnvelope.timeId.timestamp - 1),
                        nowId,
                        false,
                        5
                    ).map({ _.timeId })

                // load up to limit events scanning up to 5 windows to reach limit
                val data = windowIds.view.flatMap(
                        (windowId) => {
                            loadEvents(
                                DynamoPubSub.TopicWindow(
                                    cx.session.projectId,
                                    topic,
                                    Math.floorDiv(windowId.timestamp, DynamoPubSub.WINDOW_SIZE_MS)
                                ),
                                after,
                                nowId,
                                true,
                                limit
                            )
                        }
                    ).take(Math.max(1, Math.min(limit, PubSub.MAX_BATCH_SIZE))
                    ).flatMap({ _.event }).toSeq
                
                // Look back up to 20 seconds in the rear view
                val lookBackId = cloudutil.TimeId(new Date().getTime() - 20000)
                val rearView = windowIds.take(1
                ).map(
                    windowId => Math.floorDiv(windowId.timestamp, DynamoPubSub.WINDOW_SIZE_MS)
                ).flatMap(
                    windowNum => Seq(
                        // look back in time 1 window
                        DynamoPubSub.TopicWindow(winEnvelope.topicKey.projectId, winEnvelope.topicKey.topic, windowNum - 1),
                        DynamoPubSub.TopicWindow(winEnvelope.topicKey.projectId, winEnvelope.topicKey.topic, windowNum)
                        )
                ).flatMap(
                    topicWindow => loadEvents(topicWindow, lookBackId, nowId, false, 2000)
                ).map({ _.timeId })

                val cursor = data.lastOption.map({ _.id }).orElse(
                    windowIds.lastOption
                ).getOrElse(
                    cloudutil.TimeId.now()
                )
                // assemble a response
                PubSub.QueryResult(
                    cursor,
                    data,
                    rearView
                )
            }
        }
    }

}


object DynamoPubSub {
    val WINDOW_SIZE_MS = 1000*60  // ms in 1 minute
    // how many windows do we want to track 
    // at the 2nd "frame" level of the tree
    val WINDOW_FRAME_SIZE = 60*24 // minutes in 1 day

    /**
     * @property tableDomain uniquely identifies the cell in which 
     *     the pubsub runs - used as a prefix in dynamo table name
     */
    @inject.Singleton()
    @inject.ProvidedBy(classOf[ConfigProvider])
    case class Config(
        tableDomain:String
    ) {}

    @inject.Singleton()
    class ConfigProvider @inject.Inject() (
        @inject.name.Named("little.cell.pubsub.awsconfig") configStr:String,
        gs: gson.Gson
    ) extends inject.Provider[Config] {
        lazy val singleton: Config = {
            val js = gs.fromJson(configStr, classOf[gson.JsonObject])
            Config(
                // tableDomain should actually be the cell's domain.
                // Pubsub is scoped to a cell container.
                // Need to work Cell info into dependency injection
              js.getAsJsonPrimitive("tableDomain").getAsString()
            )
        }

        override def get():Config = singleton
    }


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
            "pubsub:" + projectId + ":" + topic + ":" + cloudutil.TimeId.pad20(timeWindow, 9)
    }

    /**
     * Given the context, topic, and time of an event - return
     * the partition key  that the event falls into, and
     * the associated window event saved to the associated window topic
     */
    def windowEnvelope(cx: cloudutil.RequestContext, eventTopic: String, eventTime: Long): (DynamoPubSub.TopicWindow, Envelope) = {
        val windowNum = Math.floorDiv(eventTime, WINDOW_SIZE_MS)
        val windowTopic = "window/" + eventTopic
        val day = Math.floorDiv(windowNum, WINDOW_FRAME_SIZE)
        val eventPk = TopicWindow(cx.session.projectId, eventTopic, windowNum)
        val windowPk = TopicWindow(cx.session.projectId, windowTopic, day)
        eventPk -> Envelope(
                windowPk,
                cloudutil.TimeId(cloudutil.TimeId.zeroId, windowNum * WINDOW_SIZE_MS),
                cx, new gson.JsonObject()
            )
    }

    case class Envelope (
        topicKey: TopicWindow,
        timeId: cloudutil.TimeId,
        cx: cloudutil.RequestContext,
        payload: gson.JsonObject
    ) {}

    case class TimeAndEvent (
        timeId: cloudutil.TimeId,
        event: Option[PubSub.Event]
    ) {}

    val topicRx = raw"[\w-]+".r
    def isTopicValid(topic:String) = null != topic && topicRx.matches(topic)
}
