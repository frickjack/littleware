package littleware.cloudmgr.service.internal

// old v1 sdk ... import com.amazonaws.services.kms
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms
import com.google.gson
import com.google.inject
import io.{jsonwebtoken => jwt}
import java.security.{ Key, PublicKey }
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Try

import littleware.cloudmgr.service.SessionMgr
import littleware.cloudmgr.service.SessionMgr.InvalidTokenException
import littleware.cloudmgr.service.littleModule
import littleware.cloudutil.{ LRN, Session }

import software.amazon.awssdk.services.kms


/**
 * @param signingKey KMS id for signing new session tokens
 * @param localMgr for verifying JWTs
 *
 * See https://www.altostra.com/blog/asymmetric-jwt-signing-using-aws-kms
 * https://sdk.amazonaws.com/java/api/latest/software/amazon/awssdk/services/kms/KmsClient.html
 */
@inject.ProvidedBy(classOf[AwsSessionMgr.Provider])
@inject.Singleton()
class AwsSessionMgr (
    signingKeyId: Option[String],
    localMgr: LocalKeySessionMgr,
    kmsClient: kms.KmsClient
    ) extends SessionMgr {

    def startSession(jwsIdToken:String, projectId:UUID, api:String):Session = localMgr.startSession(jwsIdToken, projectId, api)

    def jwsToClaims(jwsIdToken:String):Try[jwt.Claims] = localMgr.jwsToClaims(jwsIdToken)
    
    def sessionToJws(session:Session):String = {
        signingKeyId.map(
            {
                kid =>
                val b64Encoder = java.util.Base64.getUrlEncoder()
                // build our own header, since jwt.compact 
                // clears the header algorithm - ugh
                val headerJs = new gson.JsonObject()
                headerJs.addProperty(jwt.JwsHeader.KEY_ID, kid)
                headerJs.addProperty(jwt.JwsHeader.ALGORITHM, "ES256")
                val headerB64 = b64Encoder.encodeToString(headerJs.toString().getBytes("UTF-8")).replaceAll("=", "")
                val jwtNoSig = headerB64 + "." + jwt.Jwts.builder(
                    ).setClaims(SessionMgr.sessionToClaims(session)
                    ).compact().replaceAll("\\.$", "").replaceAll("^.+\\.", "")
                val req = kms.model.SignRequest.builder(
                            ).keyId(kid
                            ).signingAlgorithm(kms.model.SigningAlgorithmSpec.ECDSA_SHA_256
                            ).message(SdkBytes.fromByteArrayUnsafe(jwtNoSig.getBytes("UTF-8"))
                            ).build()
                val signBytes = kmsClient.sign(req).signature().asByteArray()
                val signB64 = b64Encoder.encodeToString(signBytes).replaceAll("=", "")
                jwtNoSig + "." + signB64
            }
        ).getOrElse(
            { throw new IllegalStateException("no signing kid registered") }
        )
    }

    def jwsToSession(jws:String):Try[Session] = localMgr.jwsToSession(jws)

    def publicKeys():Set[SessionMgr.PublicKeyInfo] = localMgr.publicKeys()
}

object AwsSessionMgr {

    @inject.Singleton()
    @inject.ProvidedBy(classOf[ConfigProvider])
    case class Config (
        oidcJwksUrl: String,
        signingKey: Option[String],
        verifyKeys: Set[String]
    ) {}

    @inject.Singleton()
    class ConfigProvider @inject.Inject() (
        @inject.name.Named("little.cloudmgr.sessionmgr.awsconfig") configStr:String,
        gs: gson.Gson
    ) extends inject.Provider[Config] {
        lazy val singleton: Config = {
            val js = gs.fromJson(configStr, classOf[gson.JsonObject])
            Config(
              js.getAsJsonPrimitive("oidcJwksUrl").getAsString(),
              Option(js.getAsJsonPrimitive("kmsSigningKey")).map({ _.getAsString() }),
              js.getAsJsonArray("kmsPublicKeys").asScala.map({ jsIt => jsIt.getAsJsonPrimitive().getAsString() }).toSet
            )
        }

        override def get():Config = singleton
    }

    @inject.Singleton()
    class Provider @inject.Inject() (
        helper:KeyHelper,
        config: Config,
        @inject.name.Named("little.cloudmgr.domain") cloud:String,
        sessionFactory:inject.Provider[Session.Builder]
    ) extends inject.Provider[AwsSessionMgr] {
        lazy val singleton:AwsSessionMgr = {
            val kmsClient = kms.KmsClient.create()
            val sessionKeys = config.verifyKeys.map(
                    { 
                        kid => 
                        kmsClient.getPublicKey(
                            kms.model.GetPublicKeyRequest.builder().keyId(kid).build()
                        )
                    }
                ).map(
                    // pem == base64 encoded der
                    { 
                        kinfo =>
                        val keyBytes = kinfo.publicKey().asByteArray()
                        helper.loadPublicKey(kinfo.keyId(), keyBytes)
                    }
                ).toSet
            val oidcKeys = helper.loadJwksKeys(new java.net.URL(config.oidcJwksUrl))
            val localMgr = new LocalKeySessionMgr(None, sessionKeys, oidcKeys, cloud, sessionFactory)
            // get the kid of the underlying key for the signing alias
            val signingKey = config.signingKey.map(
                {
                    kid =>
                    kmsClient.describeKey(kms.model.DescribeKeyRequest.builder().keyId(kid).build())
                }
            ).map(
                kinfo => kinfo.keyMetadata().arn()
            )
            new AwsSessionMgr(
                signingKey, localMgr, kmsClient
                )
        }

        def get():AwsSessionMgr = singleton
    }
}
