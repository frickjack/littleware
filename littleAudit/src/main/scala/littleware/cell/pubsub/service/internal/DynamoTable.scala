package littleware.cell.pubsub.service.internal

import com.google.gson
import com.google.{common => guava}
import com.google.inject
import java.util.Date
import java.util.UUID
import java.util.concurrent.CompletionStage;
import littleware.cell.pubsub
import littleware.cell.pubsub.service.PubSub
import littleware.cloudutil
import software.amazon.awssdk.services.dynamodb

import scala.jdk.CollectionConverters._


/**
 * Simple wrapper for our simple dynamo table
 *
 * @see https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-dynamodb-items.html
 * @see https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/examples-eventbridge.html
 * @see https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/dynamodb/package-summary.html
 * @see https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html
 * @see https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/time-to-live-ttl-before-you-start.html#time-to-live-ttl-before-you-start-formatting
 * @see scripts/dynamoTable.json, scripts/dynamo.sh
 *
 * Note: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.NamingRulesDataTypes.html
 * - dynamodb has an item size limit of 400KB
 * - dynamodb partition key max is 2048 bytes, sort key max is 1024 bytes
 * - underlying storage partition splits at 10GB on sort key
 *
 * The dynamo table schema is at `littieAudit/scripts/dynamoTable.json`
 * with a `dynamo.sh` setup script
 */
@inject.Singleton
class DynamoTable @inject.Inject() (dynamo:dynamodb.DynamoDbAsyncClient, gs:gson.Gson, config:DynamoTable.Config) {
    val tableName = config.tableDomain

    private def _saveDoc(key:DocKey,ttlSecs:Option[Int], meta:Metadata, payload:Payload):CompletionStage[UUID] = {
        if (ttlSecs.isPresent() && ttlSecs.get() < 0) {
            // validate everything ...
            return null
        }
        val putRequest = dynamodb.model.PutItemRequest.builder().item(
                //val attrMap:Map[String, dynamodb.model.AttributeValue] = 
                (
                    Map(
                        "PK" -> key.folder.toString(),
                        "SK" -> key.path,
                        "Metadata" -> gs.toJson(meta),                        
                        "Payload" -> gs.toJson(payload),
                        "TxState" -> "",
                        "TxPayload" -> ""
                    ).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                    ) ++ Map(
                        "Expiration" -> ttlSecs.map(it -> (new Date().getTime() / 1000).asInstanceOf[Int] + it).getOrElse(0),
                        "Version" -> 0
                    ).map(
                        kv -> kv._1 -> dynamodb.model.AttributeValue.builder().n(kv._2.toString()).build()
                    )
                ).asJava
            ).conditionExpression(
                "attribute_not_exists(PK)"
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


    def registerSchema() {}

    def addCabinet(projectId:UUID, name:String, actor:String):CompletionStage[UUID] = null
    def deleteCabinet(projectId:UUID, name:String):CompletionStage[UUID] = null
    def listCabinets(projectId:UUID, start:String, limit:Int):CompletionStage[ImmutableList[MetaDoc]] = null

    def addDrawer(projectId:UUID, cabinet:String, name:String, actor:String):CompletionStage[UUID] = null
    def deleteDrawer(projectId:UUID, cabinet:String, name:String, actor:String):CompletionStage[UUID] = null
    def listDrawers(projectId:UUID, cabinet:String, start:String, limit:Int):CompletionStage[ImmutableList[MetaDoc]] = null

    def addFolder(key: FolderKey, actor:String):CompletionStage[UUID] = null
    def deleteFolder(key: FolderKey, actor:String):CompletionStage[UUID] = null
    def listFolders(start: FolderKey, limit:Int):CompletionStage[ImmutableList[MetaDoc]] = null

    def addDoc(key: DocKey, ttlSecs:Option[Int], actor:String, payload:Payload):CompletionStage[UUID] = {
        if (ttlSecs.isPresent() && ttlSecs.get() < 0) {
            // validate everything ...
            return null
        }
        val putRequest = dynamodb.model.PutItemRequest.builder().item(
                //val attrMap:Map[String, dynamodb.model.AttributeValue] = 
                (
                    Map(
                        "PK" -> key.pk.toString(),
                        "SK" -> key.sk.toString(),
                        "Metadata" -> "",                        
                        "Payload" -> gs.toJson(ev.payload)
                        "TxState" -> "",
                        "TxPayload" -> ""
                    ).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                    ) ++ Map(
                        "Expiration" -> ttlSecs.map(it -> (new Date().getTime() / 1000).asInstanceOf[Int] + it).getOrElse(0),
                        "Version" -> 0
                    ).map(
                        kv -> kv._1 -> dynamodb.model.AttributeValue.builder().n(kv._2.toString()).build()
                    )
                ).asJava
            ).conditionExpression(
                "attribute_not_exists(PK)"
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


    def updateDoc(key: DocKey, ttl:Option[Int], version:Long, payload:Payload):Int = {
        0
    }

    def copyDoc(source:DocKey, dest:DocKey):Int = {
        0
    }

    def deleteDoc(key:DocKey):Int = { 0 }

    def listDocs(start:DocKey, limit:Int):CompletionStage[ImmutableList[MetaDoc]] = null
    def fetchDocs(start:DocKey, limit:Int):CompletionStage[ImmutableList[DataDoc]] = null
    
}

object DynamoTable {

    enum TxStates:
        Default, InTransaction;

    /**
     * Dynamo primary key for document storage
     */
    case class DocKey (folder: FolderKey, path: String) {
        this(
            projectId: UUID,
            cabinet: String,
            drawer: String,
            folder: String,
            path: String
        ) {
            this(FolderKey(projectId, cabinet, drawer, folder), path)
        }
        def projectId = folder.projectId
        def cabinet = folder.cabinet
        def drawer = folder.drawer
        def folder = folder.folder
    }

    case class FolderKey (
        projectId: UUID,
        cabinet: String,
        drawer: String,
        folder: String
    ) {
        lazy val isValid = Pk.isValid(this)

        override def toString():String =
            "" + projectId + "/" + cabinet + "/" + drawer + "/" + folder
    }

    object FolderKey {
        def isValid(pk:Pk) = true


    }

    case class SortKey (
        path: String
    ) {}

    trait JsonWithSchema {
        def schema: URI
        def json: gson.JsonObject
        def isValid: Boolean
    }

    object JsonWithSchema {
        def isValid(data: JsonWithSchema): Boolean = false
    }

    /**
     * Get a schema validator - ex: https://github.com/worldturner/medeia-validator/blob/master/medeia-validator-java-examples/src/main/java/com/worldturner/medeia/examples/java/gson/objects/ReadObjectExample.java
     */
    case class Payload (
        schema: URI,
        json: gson.JsonObject
    ) extends JsonWithSchema {
        lazy val isValid = JsonWithSchema.isValid(this)
    }

    case class TxState (
        schema: URI,
        json: String
    ) extends JsonWithSchema {
        override def schema: URI = null
        override def 
        lazy val isValid = JsonWithSchema.isValid(this)
    }

    case class Metadata (
        createDate: Date,
        createActor: String,
        updateDate: Date,
        updateActor: String
    ) extends JsonWithSchema {
        def schema = null
        lazy val json = null
        lazy val isValid = JsonWithSchema.isValid(this)
    }

    final case class MetaDoc (
        name: String,
        meta: Metadata
    ) {}

    
    /**
     * The cabinet, drawer, folder thing is for
     * data partitioning, security boundaries, and tree-leveling.
     */
    case class DataDoc(
        key: DocKey,
        metadata: Metadata,
        txState: TxState,
        payload: Payload,
        ttl: Long,
        lastUpdate: Date,
        lastDayAccessed: Date
    ) extends ListDoc() {}
}
