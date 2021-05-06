package littleware.cloudmgr.service

import com.google.inject
import java.util.UUID
import scala.jdk.CollectionConverters._
import scala.util.Try

import io.{ jsonwebtoken => jwt }
import java.security.{ PublicKey, PrivateKey }

import littleware.cloudutil.{LRN, Session}

/**
 * Helper manages pickling of Session and JWS -
 * including signature generation and validation
 */
@inject.ProvidedBy(classOf[SessionMgr.Provider])
@inject.Singleton()
trait SessionMgr {
    /**
     * Start a session for the given user, etc.
     *
     * @param jwsIdToken authn credential - id token for either Cognito user or little-cloud robot
     * @return session ready to encode as JWS
     */
    def startSession(jwsIdToken:String, projectId:UUID, api:String):Session

    /**
     * @return claims from validated jws
     */
    def jwsToClaims(jwsIdToken:String):Try[jwt.Claims]

    
    /**
     * Serialize a session
     */
    def sessionToJws(session:Session):String
    /**
     * Deserialize a session
     */
    def jwsToSession(jws:String):Try[Session]

    /**
     * See ex: 
     *     https://cognito-idp.us-east-2.amazonaws.com/us-east-2_860PcgyKN/.well-known/jwks.json
     * Keys for signature verification.
     */
    def publicKeys():Set[SessionMgr.PublicKeyInfo]
}


object SessionMgr {
    class InvalidTokenException (msg:String) extends IllegalArgumentException(msg) {}

    def claimsToSession(claims:jwt.Claims):Session = {
        val cloud = claims.get("little_cloud", classOf[String])
        val builder = new Session.Builder(cloud)
        builder.subject(claims.get("email", classOf[String])
        ).projectId(UUID.fromString(claims.get("little_projectid", classOf[String]))
        ).api(claims.get("little_api", classOf[String])
        ).id(UUID.fromString(claims.getId())
        ).cellId(LRN.zeroId // hard code for now - don't have cells yet
        ).iat.set(claims.getIssuedAt().getTime() / 1000L
        ).exp.set(claims.getExpiration().getTime() / 1000L
        ).isAdmin(claims.get("little_admin", classOf[String]) == "yes"
        ).authClient(claims.getAudience()
        ).build()
    }

    def sessionToClaims(session:Session):jwt.Claims = {
        jwt.Jwts.claims(
            Map(
                "email" -> session.subject,
                "little_projectid" -> session.projectId.toString(),
                "little_api" -> session.api,
                "little_cloud" -> session.lrp.cloud,
                "little_admin" -> (if (session.isAdmin) { "yes" } else { "no" }),
                "token_use" -> "little_session"
            ).asJava.asInstanceOf[java.util.Map[String,Object]]
        ).setSubject(session.subject
        ).setIssuer(session.lrp.cloud
        ).setIssuedAt(new java.util.Date(session.iat * 1000L)
        ).setExpiration(new java.util.Date(session.exp * 1000L)
        ).setId(session.id.toString()
        ).setAudience(session.authClient)
    }
    
    /**
     * key info
     *
     * @param kid should be the kms asymmetric key id or alias,
     * @param alg should be ES256 or similar,
     * @param x is the eliptic curve public key x
     * @param y is the eliptic curve public key y
     */
    case class PublicKeyInfo(
        kid: String,
        //x: String,
        //y: String,
        alg: String,
        pubKey: PublicKey
    ) {
        val use = "sig"

        override def hashCode() = kid.hashCode()
    }

    case class PrivateKeyInfo(
        kid: String,
        alg: String,
        privKey: PrivateKey
    ) {
        val use = "sig"

        override def hashCode() = kid.hashCode()
    }

    @inject.Singleton()
    class Provider @inject.Inject() (
        @inject.name.Named("little.cloudmgr.sessionmgr.type") implType:String,
        awsProvider: inject.Provider[internal.AwsKeySessionMgr],
        localProvider: inject.Provider[internal.LocalKeySessionMgr]
    ) extends inject.Provider[SessionMgr] {
        override def get():SessionMgr = implType match {
            case "local" => localProvider.get()
            case "aws" => awsProvider.get()
        }
    }
}
