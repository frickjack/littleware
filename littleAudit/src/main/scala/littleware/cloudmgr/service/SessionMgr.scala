package littleware.cloudmgr

import java.util.UUID
import scala.util.Try
import io.jsonwebtoken

import littleware.cloudutil.Session

/**
 * Helper manages pickling of Session and JWT -
 * including signature generation and validation
 */
trait SessionMgr {
    /**
     * Start a session for the given user, etc.
     *
     * @param jwtIdToken authn credential - id token for either Cognito user or little-cloud robot
     * @return session ready to encode as JWT
     */
    def startSession(jwtIdToken:String, projectId:UUID, api:String):Session

    /**
     * @return subject string from validated jwt
     */
    def validateIdToken(jwtIdToken:String):Try[String]

    /**
     * Serialize a session
     */
    def sessionToJwt(session:Session):String
    def jwsToSession(jwt:String):Try[Session]

    /**
     * See ex: 
     *     https://cognito-idp.us-east-2.amazonaws.com/us-east-2_860PcgyKN/.well-known/jwks.json
     * Keys for signature verification.
     */
    def publicKeys():Seq[SessionMgr.KeyInfo] = Nil
}


object SessionMgr {
    /**
     * key info
     *
     * @param kid should be the kms asymmetric key id or alias,
     * @param alg should be ES256 or similar,
     * @param n is the key
     */
    case class KeyInfo(
        kid: String,
        alg: String,
        n: String
    ) {
        val use = "sig"
    }
}
