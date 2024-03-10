package littleware.cabinet.service.internal

import com.google.gson
import com.google.{common => guava}
import com.google.inject
import java.net.URI
import java.net.URL
import java.util.Date
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import littleware.base.validate.ValidationException
import littleware.cabinet.service.internal.DynamoThingService.DynamoThing;
import littleware.cabinet.service.CabinetService
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ notNullValidator, positiveLongValidator }
import software.amazon.awssdk.services.dynamodb

import scala.jdk.CollectionConverters._
import scala.util.{Try, Success, Failure}


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
 * The dynamo table schema is at `littleAudit/scripts/dynamoTable.json`
 * with a `dynamo.sh` setup script
 */
@inject.Singleton
class DynamoCabinet @inject.Inject() (gs:gson.Gson, thingService:DynamoThingService) extends CabinetService {

        def createDoc(
            parent: CabinetService.Key,
            parentVersion: Long,
            name: String,
            allowNewChildren: Boolean,
            details: Option[CabinetService.Payload],
            payload: Option[CabinetService.Payload],
            actor: CabinetService.ActorInfo
        ): CompletionStage[CabinetService.Metadata] =
            this.getDoc(
                    DynamoCabinet.pk(parentKey), DynamoCabinet.sk(parentKey), false, false
                ).thenCompose(
                    _ match {
                        case Some(thing) => CompletableFuture.completedStage(thing)
                        case _ => CompletableFuture.failedStage(new AlreadyExistsException())
                    }
                ).thenApply(
                    parentThing => if(parentThing.)
                )

    def updateDoc(
            key: CabinetService.Key,
            version: Long,
            description: Option[String],
            details: Option[CabinetService.Payload],
            payload: Option[CabinetService.Payload],
            actor: CabinetService.ActorInfo
        ): CompletionStage[CabinetService.Metadata] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))

    override def deleteDoc(
        key: CabinetService.Key,
        version: Long,
        ttlSecs: Int,
        actor: CabinetService.ActorInfo
    ): CompletionStage[CabinetService.Metadata] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))

    override def lockDoc(
        key: CabinetService.Key,
        version: Long,
        lockRequest: CabinetService.LockRequest,
        actor: CabinetService.ActorInfo
     ): CompletionStage[CabinetService.Metadata] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))


    override def clearLock(
        key: CabinetService.Key,
        version: Long,
        actor: CabinetService.ActorInfo
    ): CompletionStage[CabinetService.Metadata] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))


    
    override def downloadBlob(
          key: CabinetService.Key,
          ttlSecs: Int,
          actor: CabinetService.ActorInfo
        ): CompletionStage[URL] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))

    override def startUploadBlob(
            key: CabinetService.Key,
            version: Long, ttlSecs: Int,
            actor: CabinetService.ActorInfo
        ): CompletionStage[CabinetService.UploadInfo] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))

    override def completeUploadBlob(
            key:CabinetService.Key,
            version: Long,
            success: Boolean,
            actor: CabinetService.ActorInfo
        ):CompletionStage[CabinetService.Metadata] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))


    override def fetchDocs(
            parent: CabinetService.Key,
            firstName: String,
            versionFilter: Map[String, Long],
            includeDetails: Boolean,
            includePayload: Boolean,
            limit: Int,
            actor: CabinetService.ActorInfo
        ):CompletionStage[Seq[CabinetService.DataDoc]] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))

    override def getDoc(
        key: CabinetService.Key,
        versionFilter: Option[Long],
        includeDetails: Boolean,
        includePayload: Boolean,
        actor: CabinetService.ActorInfo
    ):CompletionStage[Option[CabinetService.DataDoc]] = CompletableFuture.failedStage(new UnsupportedOperationException("not yet implemented"))
}
