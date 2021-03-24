package littleware.cloudmgr.service.internal

import com.google.inject
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import java.security.Key
import java.security.KeyPair
import java.util.UUID
import scala.util.Try

import littleware.cloudmgr.service.SessionMgr
import littleware.cloudutil.Session

/**
 * @param signingKey for signing new session tokens
 * @param verifyKeys for verifying the signature of OIDC and session tokens
 *
 * The OIDC keys for verifying cognito (or other) id tokens
 * are retrieved from jwks endpoint - ex:
 *   https://cognito-idp.us-east-2.amazonaws.com/us-east-2_860PcgyKN/.well-known/jwks.json
 */
class LocalKeySessionMgr @inject.Inject() (
    signingKey: Option[SessionMgr.PrivateKeyInfo],
    sessionKeys: Set[SessionMgr.PublicKeyInfo],
    oidcKeys: Set[SessionMgr.PublicKeyInfo]
    ) extends SessionMgr {

    def startSession(jwsIdToken:String, projectId:UUID, api:String):Session = {
        val keyInfo = signingKey getOrElse { throw new IllegalStateException("signing key not available") }
        //Jwts.parserBuilder().setSigningKey(keyInfo.privKey).build().parseClaimsJws(jws).getBody().getSubject().equals("Joe");
        //val jws = Jwts.builder().setSubject("Joe").signWith(signingKey).compact()
        
        throw new UnsupportedOperationException("not yet implemented")
    }

    def validateIdToken(jwsIdToken:String):Try[String] = Try(
        { throw new UnsupportedOperationException("not yet implemented") }
    )

    def sessionToJws(session:Session):String = {
        //val jws = Jwts.builder().setSubject("Joe").signWith(signingKey).compact()
        throw new UnsupportedOperationException("not yet implemented")
    }

    def jwsToSession(jws:String):Try[Session] = Try(
        { throw new UnsupportedOperationException("not yet implemented") }
    )

    def publicKeys():Seq[SessionMgr.PublicKeyInfo] = {
        // note: EC curve is "P-256"
        // see: https://auth0.com/docs/tokens/json-web-tokens/json-web-key-set-properties
        Nil
    }

}

object LocalKeySessionMgr {
    class Provider @inject.Inject() (helper:KeyHelper) extends inject.Provider[LocalKeySessionMgr] {
        def get():LocalKeySessionMgr = null
    }
}
