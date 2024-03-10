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
 * This service does not enforce security constraints,
 * rate-limiting, or quota model -
 * a network API implements its own security model.
 * A simple network facing cabinet service API could
 * set up security boundaries down to the
 * folder level, and partitions at folder level too.
 *
 * See Notes/explanation/cabinetService.md
 */
trait CabinetService {

    /**
     * Save a new document's data.
     * Fails if the parent document does not have an "add children" lock.
     *
     * @param parent identifies the document's parent (partition key)
     * @param parentVersion current version of the parent
     * @param name at most 128 characters - alpha-numeric, colon, dash, period, underscore
     * @param version must either be 0 for a new document, or match
     *                     the current version of an existing document
     * @param allowNewChildren whether to allow creation of child documents
     * @param details application level metadata limited to 10KB
     * @param payload data blob limited to 100KB
     * @param actor issuing the update (user id, session id, application id, ...)
     */
    def createDoc(
            parent: CabinetService.Key,
            parentVersion: Long,
            name: String,
            allowNewChildren: Boolean,
            details: Option[CabinetService.Payload],
            payload: Option[CabinetService.Payload],
            actor: CabinetService.ActorInfo
        ): CompletionStage[CabinetService.Metadata]

    /**
     * Save a document's data.  Request fails if the document is locked,
     * or if one of details, payload, or allowNewChildren is not provided
     *
     * @param key identifies the document to update
     * @param version must match the current version of an existing document
     * @param allowNewChildren whether to allow creation of child documents
     * @param details application level metadata limited to 10KB - left unchanged if not provided
     * @param payload data blob limited to 100KB - left unchanged if not provided
     * @param actor issuing the update (user id, session id, application id, ...)
     */
    def updateDoc(
            key: CabinetService.Key,
            version: Long,
            allowNewChildren: Option[Boolean],
            details: Option[CabinetService.Payload],
            payload: Option[CabinetService.Payload],
            actor: CabinetService.ActorInfo
        ): CompletionStage[CabinetService.Metadata]

    /**
     * if the document has a DeleteLock on it, and no children,
     * and no associated blob,
     * then set its ttlSecs, and update the delete lock
     * accordingly.
     */
    def deleteDoc(
        key: CabinetService.Key,
        version: Long,
        ttlSecs: Int,
        actor: CabinetService.ActorInfo
    ): CompletionStage[CabinetService.Metadata]

    /**
     * Apply the given lock, replace the existing lock if any
     * if the lock does not have a littleware reserved lock type.
     * Note that zero-project-id lock-types are reserved for
     * littleware transactions, and cannot be overwritten
     * by external clients, but must be allowed to expire.
     */
    def lockDoc(
        key: CabinetService.Key,
        version: Long,
        lockRequest: CabinetService.LockRequest,
        actor: CabinetService.ActorInfo
     ): CompletionStage[CabinetService.Metadata]

    /**
     * Clear client-managed lock if any
     */
    def clearLock(
        key: CabinetService.Key,
        version: Long,
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
     * Lock document, and get a URL authenticated to upload a blob associated
     * with this document.  Fails if the document is locked.
     *
     * @param ttlSecs time to lock the document for upload -
     *        and for the signed url to be valid -
     *        upload fails if does not complete before now + ttlSecs -
     *        between 90 seconds and one hour
     */
    def startUploadBlob(
            key: CabinetService.Key,
            version: Long,
            ttlSecs: Int,
            actor: CabinetService.ActorInfo
        ):CompletionStage[CabinetService.UploadInfo];

    /**
     * Report a blob upload complete
     *
     * @param actor info for updating lastDayAccessed
     */
    def completeUploadBlob(
            key:CabinetService.Key,
            version: Long,
            success: Boolean,
            actor: CabinetService.ActorInfo
        ):CompletionStage[CabinetService.Metadata];

    /**
     * Fetching a doc may eventually update the lastDayAccessed information on the
     * parent if that data has not been updated in the last 12 hours.
     * Note that lastDayAccessed updates do not change the document version.
     *
     * @param limit if set to 0 or less, then only return an exact match against firstName,
     *    otherwise return up to limit records or 2MB of data - whichever comes first
     * @param versionFilter specifies the versions that the client already has
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

    /**
     * fetchMetadata gets metadata without updating access date
     */
    def fetchMetadata(
        parent: CabinetService.Key
    ):CompletionStage[Seq[CabinetService.Metadata]]

    /**
     * Basically like fetchDocs, but looks for an exact match
     * for the given document key
     */
    def getDoc(
            key: CabinetService.Key,
            versionFilter: Option[Long],
            includeDetails: Boolean,
            includePayload: Boolean,
            actor: CabinetService.ActorInfo
        ):CompletionStage[Option[CabinetService.DataDoc]];

}

object CabinetService {
    /**
     * Document key of form lrp://$cloud/cabinet/$projectId/document/$drawer/$documentPath
     */
    sealed trait Key {
        val lrn:cloudutil.LRPath
        val documentName:String

        def parentKey:Option[Key]
    }

    object Key {
        private case class SimpleKey(lrn: cloudutil.LRPath) extends Key {
            override lazy val parentKey = Option(
                    lrn.path.replaceAll("/[^/]+$")
                ).filter(
                    pathStr => !pathStr.isEmpty() && pathStr != lrn.path
                ).map(
                    parentPathStr => builder().lrn(
                            cloudutil.LRN.pathBuilder().copy(lrn).path(parentPathStr).build()
                        ).build()
                )
        }

        def keyValidator(lrn:cloudutil.LRPath, name:String):Option[String] = NotNullValidator(lrn, name) orElse {
            Option.when("cabinet" != lrn.api)(
                s"${name} has non-cabinet api"
            ) orElse Option.when("document" != lrn.resourceType)(
                s"${name} has non-document resource type"
            )
        }

        final class Builder private[Key]() extends PropertyBuilder[Key] {
            val lrn = new Property[cloudutil.LRPath](null) withName "lrn" withValidator keyValidator
            
            override def copy(value: Key):this.type = lrn(value.lrn)

            override def build(): Key = {
                this.validate()
                SimpleKey(lrn())
            }
        }

        def builder() = new Builder()
    }

    sealed trait Payload {
        val schema: URI
        val json: gson.JsonObject
    }

    object Payload {
        private final case class SimplePayload (
            schema: URI,
            json: gson.JsonObject
        ) extends Payload {}


        class Builder private () extends PropertyBuilder[Payload] {
            val schema = new Property[URI](null) withName "schema" withValidator notNullValidator
            val json = new Property[gson.JsonObject] withName "json" withValidator notNullValidator

            override def copy(value:Payload):Builder =
                schema(value.schema).json(value.json)

            override def build(): Payload = {
                this.validate()
                SimplePayload(schema, json)
            }
        }

        def builder(): Builder = new Builder()

        val emptyUri = URI.create("littleware://json-schema/empty")
        val emptyPayload:Payload = builder().schema(emptyUri).json(new gson.JsonObject()).build()
    }


    /**
     * Internal information collected about a blob by CabinetService.completeUploadBlob
     *
     * @param checksum has form object-store-type://value
     */
    sealed trait BlobInfo {
        val sizeBytes: Long
        val checksum: URI
        val lastUpload: AccessInfo
        val lastDayAccessed: AccessInfo
        val schema: URI
    }

    sealed trait BlobInfoWithExpire extends BlobInfo {
        val expireDate: Option[Date]
    }

    /**
     * Application layer info about the actor,
     * session, request, or whatever
     */
    sealed trait ActorInfo {
        val actorId: URI
        val requestId: URI
        val details: Payload
    }

    /**
     * Client supplied request to put a lock on a document
     * for ttlSecs (min 1 second, max 900)
     */
    sealed trait LockRequest {
        val lockType: URI
        val description: String
        val details: Payload
        val ttlSecs: Int
    }

    /**
     * Hold AddChildrenLockType on parent to mark add-children transaction
     */
    val AddChildrenLockType = java.net.URI.create("lrn://littleware/cabinet/" + lrn.zeroId + "/lock-type/global/add-children-lock")
    /**
     * Delete lock type on parent to setup a delete-node transaction
     */
    val DeleteLockType = java.net.URI.create("lrn://littleware/cabinet/" + lrn.zeroId + "/lock-type/global/delete-lock")
    /**
     * Generic lock type for client managed transactions
     */
    val GenericLockType = java.net.URI.create("lrn://littleware/cabinet/" + lrn.zeroId + "/lock-type/global/generic-lock")
    /**
     * UploadLockType for blob-upload transaction
     */
    val UploadLockType = java.net.URI.create("lrn://littleware/cabinet/" + lrn.zeroId + "/lock-type/global/blob-upload-lock")

    object LockRequest {
        private case class SimpleLockRequest(
            lockType: URI,
            description: String,
            details: Payload,
            ttlSecs: Int
        ) extends LockRequest {}
        
        class Builder private() extends PropertyBuilder[LockRequest] {
            val lockType = new Property[URI](null) withName "lockType" withValidator notNullValidator
            val description = new Property[String]("") withName "description" withValidator notNullValidator
            val details = new Property[Payload](null) withName "payload" withValidator notNullValidator
            val ttlSecs = new Property[Int](0) withName "ttlSecs"

        }
    }

    /**
     * Returns information about lock managed by the cabinet service.
     */
    sealed trait LockInfo {
        val id: UUID
        val lockType: URI
        val created: AccessInfo
        val lastEdit: AccessInfo
        val expireDate: Date
        val description: String
        val details: Payload
    }

    sealed trait UploadInfo {
        val uploadUrl: URL
        val metadata: Metadata
    }

    /**
     * Tracks the date and actor who accessed the
     * resource in the last 24 hours, and the
     * version number of the asset when accessed.
     */
    sealed trait AccessInfo {
        def date: Date;
        def actor: ActorInfo;
        def version: Long;
    }

    /**
     * Data maintained by the cabinet system.
     */
    sealed trait Metadata {
        val key: Key
        val created: AccessInfo
        val lastEdit: AccessInfo
        //
        // TODO - integrate with audit-log service, but simple
        // lastDayAccessed is useful for liveness, but you can't
        // check it without changing it ... so maybe not
        //
        val lastDayAccessed: AccessInfo

        // specifying version on save supports simple conditional updates
        // for transactions/locking/etc
        val version: Long
        val lockInfo: Option[LockInfo]
        val blobInfo: Option[BlobInfoWithExpire]
        val payloadInfo: Option[BlobInfo]
        val detailsInfo: Option[BlobInfo]
        val allowNewChildren: Boolean;
        // see https://stackoverflow.com/questions/14077414/dynamodb-increment-a-key-value
        // Q: how to handle children with expire dates?
        // don't do this - at least for now
        // val numChildren: Int;
        val expireDate: Option[Date];
    }

    object Metadata {
        /**
         * TODO: expose this as a trait with builder, and move the implementation private
         */
        private final case class SimpleMetadata (
            key: Key,
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
            allowNewChildren: Boolean,
            expireDate: Option[Date]
        ) extends Payload with Metadata {
            override def schema = URI.create("https://frickjack.com/schema/cabinet/metadata/v1")
            lazy val json = null
            lazy val isValid = Payload.isValid(this)
        }

        final class Builder private () extends PropertyBuilder[Metadata] {
            val key = new Property[Key](null) withName "key" withValidator notNullValidator
            val created = new Property[AccessInfo](null) withName "created" withValidator notNullValidator
            val lastEdit = new Property[AccessInfo](null) withName "lastEdit" withValidator notNullValidator
            val lastDayAccessed = new Property[AccessInfo](null) withName "lastDayAccessed" withValidator notNullValidator
            // specifying version on save supports simple conditional updates
            // for transactions/locking/etc
            val version = new Property[Long](0L) withName "version" withValidator positiveLongValidator
            val lockInfo = new OptionProperty[LockInfo]() withName "lockInfo"
            val blobInfo = new OptionProperty[BlobInfoWithExpire]() withName "blobInfo"
            val payloadInfo = new OptionProperty[BlobInfo]() withName "payloadInfo"
            val detailsInfo = new OptionProperty[BlobInfo]() withName "detailsInfo"
            val allowNewChildren = new Property[Boolean](false) withName "allowNewChildren" withValidator notNullValidator;
            val expireDate: Option[Date] = new OptionProperty[Date]() withName "expireDate"
            
            override def copy(value: Metadata): this.type = this.key(value.key
                ).created(value.created
                ).lastEdit(value.lastEdit
                ).lastDayAccessed(value.lastDayAccessed
                ).version(value.version
                ).lockInfo(value.lockInfo
                ).blobInfo(value.blobInfo
                ).payloadInfo(value.payloadInfo
                ).detailsInfo(value.detailsInfo
                ).allowNewChildren(value.allowNewChildren
                ).expireDate(value.expireDate)

            override def build(): Metadata = {
                this.validate()
                Metadata(
                    key(), created(), lastEdit(), lastDayAccessed(),
                    version(), lockInfo(), blobInfo(),
                    payloadInfo(), detailsInfo(),
                    allowNewChildren(), expireDate()
                )
            }
        }

        def builder():Builder = new Builder()
    }

    
    /**
     * The cabinet, drawer, folder thing is for
     * data partitioning, security boundaries, and tree-leveling.
     * Note that details and payload are not set in multiple
     * circumstances: 
     * - the client did not request them <br>
     * - the client indicated that it already has the current version <br>
     */
    final case class DataDoc(
        name: String,
        version: Long,
        metadata: Metadata,
        details: Option[Payload],
        payload: Option[Payload]
    ) {}
}
