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
import littleware.cabinet.service.CabinetService
import littleware.scala.PropertyBuilder
import littleware.scala.PropertyBuilder.{ notNullValidator, positiveLongValidator }
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
class DynamoThingService @inject.Inject() (gs:gson.Gson, config:DynamoThingService.Config) {
    private val dynamo = config.dynamo
    private val tableName = config.tableDomain

    /**
     * Internal create new entry to dynamo -
     * see littleAudit/scripts/dynamoTable.json
     * Does no validation on inputs, but specifies version based
     * conditions on the dynamo put.
     */
    private[internal] def putThing(
        thing:DynamoThingService.DynamoThing
    ):CompletionStage[DynamoThingService.DynamoThing] = {
        if (thing.newVersion != 1) {
            throw new IllegalArgumentException("putThing version must be 1")
        }
        val putRequest = dynamodb.model.PutItemRequest.builder()
            .tableName(tableName)
            .item(
                //val attrMap:Map[String, dynamodb.model.AttributeValue] = 
                (
                    (
                        Seq(
                            "Details" -> thing.details,
                            "Payload" -> thing.payload
                        ).filter(_._2.isDefined
                        ).map(kv => kv._1 -> kv._2.get
                        ).toMap ++
                        Map(
                            // string attributes
                            "PK" -> thing.pk,
                            "SK" -> thing.sk,
                            "Metadata" -> thing.metadata,
                        )
                    ).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                    ) ++ Map(
                        // numeric attributes
                        "Expiration" -> thing.expirationUnixTime,
                        "Version" -> thing.newVersion
                    ).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().n(kv._2.toString()).build()
                    )
                ).asJava
            ).conditionExpression(
                "attribute_not_exists(PK)"
            ).build()
        dynamo.putItem(putRequest).thenApply(
                response => thing
            )
    }

    /**
     * Does no validation on inputs, but specifies version based
     * conditions on the dynamo put.
     */
    private[internal] def updateThing(
        thing:DynamoThingService.DynamoThing
    ):CompletionStage[DynamoThingService.DynamoThing] = {
        if (thing.newVersion < 2) {
            throw new IllegalArgumentException("updateThing version must be greater than 1")
        }

        val updateMap = (
                        Seq(
                            "Details" -> thing.details,
                            "Payload" -> thing.payload
                        ).filter(_._2.isDefined
                        ).map(kv => kv._1 -> kv._2.get
                        ).toMap ++
                        Map(
                            "Metadata" -> thing.metadata,
                        )
                    ).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                    ) ++ Map(
                        // numeric attributes
                        "Expiration" -> thing.expirationUnixTime,
                        "Version" -> thing.newVersion
                    ).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().n(kv._2.toString()).build()
                    )
                
        val updateRequest = dynamodb.model.UpdateItemRequest.builder()
            .tableName(tableName
            ).key(
                Map(
                    "PK" -> thing.pk,
                    "SK" -> thing.sk,
                ).map(
                    kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                ).asJava
            ).updateExpression(
                updateMap.map(_._1
                  ).foldLeft((new StringBuilder("SET "), "")
                  )(
                    (pair, key) => pair match {
                        case (sb, prefix) => (sb.append(prefix).append(key).append("=:").append(key), ", ")
                    }
                  )._1.toString()
            ).expressionAttributeValues(
                (
                    updateMap.map(kv => ":" + kv._1 -> kv._2) ++
                    Map(":CurrentVersion" -> (thing.newVersion - 1)).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().n(kv._2.toString()).build()
                    )
                ).asJava
            ).conditionExpression(
                "Version = :CurrentVersion"
            ).build()
        dynamo.updateItem(updateRequest).thenApply(
                response => thing
            )
    }


    /**
     * Does no validation on inputs, but requires thing to exist
     *
     * @return true if the thing exists and is updated
     */
    private[internal] def updateAccessInfo(
        pk:String, sk:String, accessInfo: String
    ):CompletionStage[Boolean] = {
        CompletableFuture.failedStage(
            new UnsupportedOperationException("not yet implemented")
        )
    }

    private[internal] def getThing(
        pk: String,
        sk: String,
        withDetails: Boolean,
        withPayload: Boolean
    ):CompletionStage[Option[DynamoThingService.DynamoThing]] = {
        val getRequest = dynamodb.model.GetItemRequest.builder()
            .tableName(tableName)
            .key(
                //val attrMap:Map[String, dynamodb.model.AttributeValue] = 
                (
                    Map(
                        // string attributes
                        "PK" -> pk,
                        "SK" -> sk
                    ).map(
                        kv => kv._1 -> dynamodb.model.AttributeValue.builder().s(kv._2).build()
                    )
                ).asJava
            ).projectionExpression(
                "PK,SK,Version,Expiration,Metadata" + Option.when(withDetails)(",Details").getOrElse("") + Option.when(withPayload)(",Payload").getOrElse("")
            ).build()
        dynamo.getItem(getRequest).thenApply(
            response => Option.when(response.hasItem())(response.item)
                .map(
                    item => DynamoThingService.DynamoThing(
                        item.get("PK").s(),
                        item.get("SK").s(),
                        item.get("Metadata").s(),
                        Option(item.get("Details")).map(_.s()),
                        Option(item.get("Payload")).map(_.s()),
                        item.get("Version").n().toLong,
                        item.get("Expiration").n().toLong
                    )
                )
            )
    }
}

object DynamoThingService {
    /**
     * @property tableDomain uniquely identifies the cell in which 
     *     the pubsub runs - used as a prefix in dynamo table name
     */
    @inject.Singleton()
    @inject.ProvidedBy(classOf[ConfigProvider])
    case class Config(
        tableDomain:String,
        dynamo:dynamodb.DynamoDbAsyncClient
    ) {}

    @inject.Singleton()
    class ConfigProvider @inject.Inject() (
        @inject.name.Named("little.cabinet.dynamo.awsconfig") configStr:String,
        gs: gson.Gson
    ) extends inject.Provider[Config] {
        lazy val singleton: Config = {
            val js = gs.fromJson(configStr, classOf[gson.JsonObject])
            val client = Option(js.getAsJsonPrimitive("endpointOverride").getAsString()).map(
                endpoint => dynamodb.DynamoDbAsyncClient.builder()
                    .endpointOverride(URI.create(endpoint))
                    // The region is meaningless for local DynamoDb but required for client builder validation
                    //.region(Region.US_EAST_1)
                    //.credentialsProvider(
                    //    StaticCredentialsProvider.create(
                    //        AwsBasicCredentials.create("dummy-key", "dummy-secret")
                    //        )
                    //)
                    .build()
            ).getOrElse(
                dynamodb.DynamoDbAsyncClient.create()
            )

            Config(
                //
                // tableDomain should actually be the cell's domain.
                // Pubsub is scoped to a cell container.
                // Need to work Cell info into dependency injection
                //
              js.getAsJsonPrimitive("tableDomain").getAsString(),
              client
            )
        }

        override def get():Config = singleton
    }

    /**
     * see littleAudit/scripts/dynamoTable.json
     */
    private[internal] case class DynamoThing(
        pk: String,
        sk: String,
        metadata: String,
        details: Option[String],
        payload: Option[String],
        newVersion: Long,
        expirationUnixTime: Long,
        accessInfo: String
    ) {}

    private[internal] object DynamoThing {
        private [internal] final class Builder extends PropertyBuilder[DynamoThing] {
            val pk = new Property[String](null) withName "pk" withValidator pathLikeValidator
            val sk = new Property[String](null) withName "sk" withValidator rxValidator(raw"[\w\.-_]{1,255}".r)
            val metadata = new Property[String](null) withName "metadata" withValidator notEmptyValidator
            val details = new OptionProperty[String]() withName "details" withMemberValidator notEmptyBelowMaxValidator(10*1024)
            val payload = new OptionProperty[String]() withName "payload" withMemberValidator notEmptyBelowMaxValidator(100*1024)
            val newVersion = new Property[Long](0L) withName "newVersion" withValidator positiveLongValidator
            val expirationUnixTime = new Property[Long](0L) withName "expirationUnixTime"
            val accessInfo = new Property[String](null) withName "accessInfo" withValidator notEmptyValidator

            override def copy(value: DynamoThing): this.type =
                pk(value.pk).sk(value.sk).metadata(value.metadata
                ).details(value.details).payload(value.payload
                ).newVersion(value.newVersion
                ).expirationUnixTime(value.expirationUnixTime
                ).accessInfo(value.accessInfo)

            override def build(): DynamoThing = {
                this.validate()
                DynamoThing(
                    pk(), sk(), metadata(), details(), payload(),
                    newVersion(), expirationUnixTime(), accessInfo()
                    )
            }
        }

        def builder() = new Builder()
    }

}
