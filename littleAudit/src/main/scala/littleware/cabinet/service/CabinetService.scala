package littleware.cabinet.service

import com.google.gson
import com.google.{common => guava}
import com.google.inject
import java.net.URI
import java.net.URL
import java.util.Date
import java.util.UUID
import java.util.concurrent.CompletionStage;
import littleware.cloudutil
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ emailValidator, notNullValidator, positiveLongValidator }


/**
 * Cabinet interface for generic dynamo interaction.
 * This service does not enforce security constraints -
 * a network API implements its own security model.
 * A simple network facing cabinet service API could
 * set up security boundaries down to the
 * folder level, and partitions at folder level too.
 *
 * See Notes/explanation/cabinetService.md
 */
trait CabinetService {
    /**
     * Save a document's data.  Request fails if the request does not specify one
     * of ttlSecs, description, details, payload, or lockRequest.
     *
     * @param parent identifies the document's parent (partition key)
     * @param name at most 128 characters - alpha-numeric, colon, dash, period, underscore
     * @param version must either be 0 for a new document, or match
     *                     the current version of an existing document
     * @param description at most 128 characters, default to empty string if version 0
     * @param ttlSecs to auto-delete the document after, or negative value to clear
     * @param actor issuing the update (user id, session id, application id, ...)
     * @param details application level metadata limited to 10KB - left unchanged if not provided
     * @param payload data blob limited to 100KB - left unchanged if not provided
     * @param lockRequest to acquire a new lock on the document - set ttl to zero to unlock -
     *     if not provided, and the document has an active lock attached, then the save request fails
     */
    def saveDoc(
            parent: CabinetService.Key,
            version:Long,
            description: Option[String],
            ttlSecs:Option[Int],
            details: Option[CabinetService.Payload],
            payload: Option[CabinetService.Payload],
            lockRequest: Option[CabinetService.LockRequest],
            actor: CabinetService.ActorInfo
        ): CompletionStage[CabinetService.Metadata]
    
    /**
     * Get a URL authenticated to download the blob (if any)
     * associated with this document
     *
     * @param ttlSecs lifetime of URL - between 30 seconds and one hour
     * @param actor info for updating lastDayAccessed
     */
    def downloadBlob(key: CabinetService.Key, ttlSecs: Int, actor: CabinetService.ActorInfo): CompletionStage[URL];

    /**
     * Get a URL authenticated to upload a blob associated
     * with this document.  Fails if the document is locked.
     *
     * @param ttlSecs time to lock the document for upload -
     *        and for the signed url to be valid -
     *        upload fails if does not complete before now + ttlSecs -
     *        between 90 seconds and one hour
     */
    def startUploadBlob(
            key: CabinetService.Key,
            version: Long, ttlSecs: Int,
            actor: CabinetService.ActorInfo
        ):CompletionStage[CabinetService.UploadInfo];

    /**
     * Report a blob upload complete
     *
     * @param ttlHours lifetime before deleting the blob
     * @param actor info for updating lastDayAccessed
     */
    def completeUploadBlob(
            key:CabinetService.Key,
            version: Long,
            success: Boolean,
            actor: CabinetService.ActorInfo
        ):CompletionStage[CabinetService.Metadata];

    /**
     * Fetching a doc may update the lastDayAccessed information on the document
     * and its parents (if any) if that data has not been updated in the last 12 hours.
     *
     * @param limit if set to 0 or less, then only return an exact match against firstName,
     *    otherwise return up to limit records or 2MB of data - whichever comes first
     * @param actor for lastDayAccessed updates
     */
    def fetchDocs(
            parent: CabinetService.Key,
            firstName: String,
            versionFilter: Map[String, Long],
            includeDetails: Boolean,
            includePayload: Boolean,
            limit: Int,
            actor: CabinetService.ActorInfo
        ):CompletionStage[Seq[CabinetService.DataDoc]];
}

object CabinetService {
    /**
     * Document key
     *
     * @property uri of form cabinet://$projectId/$closet/$cabinet/$drawer/$folder/$document
     */
    trait Key {
        def uri:URI
    }

    object Key {
        private case class SimpleKey(uri: URI) extends Key {}

        final class Builder private[Key]() extends PropertyBuilder[Key] {
            val uri = new Property[URI](null) withName "uri" withValidator notNullValidator
            
            override def copy(value: Key):this.type = this
            override def build(): Key = {
                this.validate()
                SimpleKey(uri())
            }
        }

        def builder() = new Builder()
    }

    private[service] trait InternalKey extends Key {}

    trait JsonWithSchema {
        def schema: URI
        def json: gson.JsonObject
    }

    object JsonWithSchema {
        def isValid(schema: URI, json: gson.JsonObject): Boolean = false
        def isValid(value: JsonWithSchema): Boolean = false
    }

    /**
     * Data maintained by the client application
     */
    final case class Payload (
        schema: URI,
        json: gson.JsonObject
    ) extends JsonWithSchema {
    }


    /**
     * Internal information collected about a blob by CabinetService.completeUploadBlob
     *
     * @param checksum has form object-store-type://value
     */
    trait BlobInfo {
        val sizeBytes: Long
        val checksum: URI
        val lastUpload: AccessInfo
        val lastDayAccessed: AccessInfo
        val schema: URI
    }

    trait BlobInfoWithExpire extends BlobInfo {
        val expireDate: Option[Date]
    }

    /**
     * Application layer info about the actor,
     * session, request, or whatever
     */
    trait ActorInfo {
        val actorId: URI
        val requestId: URI
        val details: Payload
    }

    trait LockRequest {
        val lockType: URI
        val description: String
        val details: Payload
        val ttlSecs: Int
    }

    /**
     * Information about lock managed by the cabinet service.
     */
    trait LockInfo {
        val lockType: URI
        val created: AccessInfo
        val lastEdit: AccessInfo
        val expireDate: Date
        val description: String
        val details: Payload
    }

    trait UploadInfo {
        val uploadUrl: URL
        val metadata: Metadata
    }

    /**
     * Tracks the date and actor who accessed the
     * resource in the last 24 hours, and the
     * version number of the asset when accessed.
     */
    trait AccessInfo {
        def date: Date;
        def actor: ActorInfo;
        def version: Long;
    }

    /**
     * Data maintained by the cabinet system.
     */
    trait Metadata {
        def created: AccessInfo;
        def lastEdit: AccessInfo;
        def lastDayAccessed: AccessInfo;
        // specifying version on save supports simple conditional updates
        // for transactions/locking/etc
        def version: Long;
        def lockInfo: Option[LockInfo];
        def blobInfo: Option[BlobInfoWithExpire];
        def payloadInfo: Option[BlobInfo];
        def detailsInfo: Option[BlobInfo];
        def name: String;
        def description: String;
        // see https://stackoverflow.com/questions/14077414/dynamodb-increment-a-key-value
        // Q: how to handle children with expire dates?
        def numChildren: Int;
        // do not allow a document with an expire date to have children
        def expireDate: Option[Date];
    }

    object Metadata {
        /**
         * TODO: expose this as a trait with builder, and move the implementation private
         */
        private final case class SimpleMetadata (
            created: AccessInfo,
            lastEdit: AccessInfo,
            lastDayAccessed: AccessInfo,
        // specifying version on save supports simple conditional updates
        // for transactions/locking/etc
            version: Long,
            lockInfo: Option[LockInfo],
            blobInfo: Option[BlobInfoWithExpire],
            payloadInfo: Option[BlobInfo],
            detailsInfo: Option[BlobInfo],
            name: String,
            description: String,
        // see https://stackoverflow.com/questions/14077414/dynamodb-increment-a-key-value
            numChildren: Int,
            expireDate: Option[Date]
        ) extends JsonWithSchema with Metadata {
            override def schema = URI.create("https://frickjack.com/schema/cabinet/metadata/v1")
            lazy val json = null
            lazy val isValid = JsonWithSchema.isValid(this)

        }

        final class Builder(
            defaultCloud:String,
            expectedApi:String,
            expectedResourceType:String,
            defaultState:String,
            stateSet:Set[String]
        ) extends PropertyBuilder[Metadata] {
            override def copy(value: Metadata): this.type = this
            override def build(): Metadata = throw new UnsupportedOperationException()
        }
    }

    
    /**
     * The cabinet, drawer, folder thing is for
     * data partitioning, security boundaries, and tree-leveling.
     */
    final case class DataDoc(
        name: String,
        version: Long,
        metadata: Metadata,
        details: Option[Payload],
        payload: Option[Payload]
    ) {}
}
