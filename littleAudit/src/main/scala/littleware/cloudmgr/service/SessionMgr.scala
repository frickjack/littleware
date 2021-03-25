package littleware.cloudmgr.service

import java.util.UUID
import scala.util.Try
import io.jsonwebtoken
import java.security.{ PublicKey, PrivateKey }

import littleware.cloudutil.Session

/**
 * Helper manages pickling of Session and JWS -
 * including signature generation and validation
 */
trait SessionMgr {
    /**
     * Start a session for the given user, etc.
     *
     * @param jwsIdToken authn credential - id token for either Cognito user or little-cloud robot
     * @return session ready to encode as JWS
     */
    def startSession(jwsIdToken:String, projectId:UUID, api:String):Session

    /**
     * @return subject string from validated jws
     */
    def validateIdToken(jwsIdToken:String):Try[String]

    /**
     * Serialize a session
     */
    def sessionToJws(session:Session):String
    def jwsToSession(jws:String):Try[Session]

    /**
     * See ex: 
     *     https://cognito-idp.us-east-2.amazonaws.com/us-east-2_860PcgyKN/.well-known/jwks.json
     * Keys for signature verification.
     */
    def publicKeys():Set[SessionMgr.PublicKeyInfo]
}


object SessionMgr {
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
}
