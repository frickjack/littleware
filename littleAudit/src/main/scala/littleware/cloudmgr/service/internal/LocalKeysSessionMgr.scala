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
import littleware.cloudmgr.service.littleModule
import littleware.cloudutil.Session

/**
 * @param signingKey for signing new session tokens
 * @param verifyKeys for verifying the signature of OIDC and session tokens
 *
 * The OIDC keys for verifying cognito (or other) id tokens
 * are retrieved from jwks endpoint - ex:
 *   https://cognito-idp.us-east-2.amazonaws.com/us-east-2_860PcgyKN/.well-known/jwks.json
 */
@inject.ProvidedBy(classOf[LocalKeySessionMgr.Provider])
@inject.Singleton()
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

    def publicKeys():Set[SessionMgr.PublicKeyInfo] = {
        // note: EC curve is "P-256"
        // see: https://auth0.com/docs/tokens/json-web-tokens/json-web-key-set-properties
        sessionKeys
    }

}

object LocalKeySessionMgr {
    class Provider @inject.Inject() (helper:KeyHelper, config:littleModule.Config) extends inject.Provider[LocalKeySessionMgr] {
        lazy val singleton:Option[LocalKeySessionMgr] =
            Option(config.localSessionMgrConfig).map(
                {
                    lc => 
                    val signingKey = lc.signingKey.map({ kid2pem => helper.loadPrivateKey(kid2pem.kid, kid2pem.pem) })
                    val sessionKeys = lc.verifyKeys.map({ kid2pem => helper.loadPublicKey(kid2pem.kid, kid2pem.pem) }).toSet
                    val oidcKeys = helper.loadJwksKeys(new java.net.URL(lc.oidcJwksUrl))
                    new LocalKeySessionMgr(signingKey, sessionKeys, oidcKeys)
                }
            ) orElse Option(new LocalKeySessionMgr(None, Set.empty, Set.empty))

        def get():LocalKeySessionMgr = singleton.get
    }
}
