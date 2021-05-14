package littleware.cloudmgr.service.internal

import com.google.gson
import com.google.inject
import io.{jsonwebtoken => jwt}
import java.security.{ Key, PublicKey }
import java.util.{ Date, UUID }
import scala.jdk.CollectionConverters._
import scala.util.Try

import littleware.cloudmgr.service.SessionMgr
import littleware.cloudmgr.service.SessionMgr.InvalidTokenException
import littleware.cloudmgr.service.littleModule
import littleware.cloudutil.{ LRN, Session }

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
class LocalKeySessionMgr (
    signingKey: Option[SessionMgr.PrivateKeyInfo],
    sessionKeys: Set[SessionMgr.PublicKeyInfo],
    oidcKeys: Set[SessionMgr.PublicKeyInfo],
    cloud:String,
    sessionFactory:inject.Provider[Session.Builder]
    ) extends SessionMgr {

    val resolver = new jwt.SigningKeyResolverAdapter() {
        override def resolveSigningKey(jwsHeader:jwt.JwsHeader[T] forSome { type T <: jwt.JwsHeader[T] }, claims:jwt.Claims):java.security.Key = {
            val kid = jwsHeader.getKeyId()
            (
                {
                    if (claims.getIssuer() == cloud) {
                        sessionKeys
                    } else {
                        oidcKeys
                    }
                }
            ).find(
                { it => it.kid == kid }
            ).map(
                { _.pubKey }
            ) getOrElse {
                throw new SessionMgr.InvalidTokenException(s"invalid auth kid ${kid}")
            }
        }
    }

    def startSession(jwsIdToken:String, projectId:UUID, api:String):Session = {
        val claims = jwsToClaims(jwsIdToken).get
        val builder = sessionFactory.get(
            ).subject(claims.get("email", classOf[String])
            )
        if(claims.getIssuer() == cloud) {
            if (claims.getAudience() != s"apikeys@${cloud}") {
                throw new InvalidTokenException(s"non api key session request with session token from ${builder.subject()}")
            }
            val session = SessionMgr.claimsToSession(claims)
            if (session.projectId != projectId) {
                throw new InvalidTokenException(s"api key not in same project as requested access")
            }
            // get admin/robot settings, etc
            builder.isAdmin(session.isAdmin)
        }
        val oneHourFromNow = new Date(new Date().getTime() + 60*1000)
        builder.projectId(projectId).api(api
        ).id(UUID.randomUUID()
        ).cellId(LRN.zeroId // hard code for now - don't have cells yet
        ).iat.set(new Date().getTime() / 1000L
        ).exp.set(oneHourFromNow.getTime() / 1000L
        ).build()
    }

    def jwsToClaims(jwsIdToken:String):Try[jwt.Claims] = Try(
        { 
            jwt.Jwts.parserBuilder(
            ).setSigningKeyResolver(resolver
            ).setAllowedClockSkewSeconds(24*60*60 // allow 24 hours for now
            ).build(
            ).parseClaimsJws(jwsIdToken
            ).getBody()
        }
    ).flatMap( claims => Try( {
                    Seq("email", jwt.Claims.EXPIRATION, jwt.Claims.ISSUER, jwt.Claims.ISSUED_AT, jwt.Claims.AUDIENCE).foreach({
                        key =>
                        if(claims.get(key) == null) {
                            throw new InvalidTokenException(s"missing ${key} claim")
                        }
                    })
                    claims
                }
            )
    )


    def sessionToJws(session:Session):String = {
        val signingInfo = signingKey getOrElse { throw new UnsupportedOperationException("signing key not available") }
        //val jws = Jwts.builder().setSubject("Joe").signWith(signingKey).compact()
                // build a session token
        jwt.Jwts.builder(
        ).setHeaderParam(jwt.JwsHeader.KEY_ID, signingInfo.kid
        ).setClaims(SessionMgr.sessionToClaims(session)
        ).signWith(signingInfo.privKey
        ).compact()
    }

    def jwsToSession(jws:String):Try[Session] = jwsToClaims(jws
        ) map { claims => SessionMgr.claimsToSession(claims) }

    def publicKeys():Set[SessionMgr.PublicKeyInfo] = {
        // note: EC curve is "P-256"
        // see: https://auth0.com/docs/tokens/json-web-tokens/json-web-key-set-properties
        sessionKeys
    }

}

object LocalKeySessionMgr {
    @inject.Singleton()
    @inject.ProvidedBy(classOf[ConfigProvider])
    case class Config (
        oidcJwksUrl: String,
        signingKey: Option[Kid2Pem],
        verifyKeys: Set[Kid2Pem]
    ) {}

    case class Kid2Pem (kid:String, pem:String) {
        def this(json:gson.JsonObject) = this(
            json.getAsJsonPrimitive("kid").getAsString(),
            json.getAsJsonPrimitive("pem").getAsString()
        )

        override def hashCode():Int = kid.hashCode()
    }

    @inject.Singleton()
    class ConfigProvider @inject.Inject() (
        @inject.name.Named("little.cloudmgr.sessionmgr.localconfig") configStr:String,
        gs: gson.Gson
    ) extends inject.Provider[Config] {
        lazy val singleton: Config = {
            val js = gs.fromJson(configStr, classOf[gson.JsonObject])
            Config(
              js.getAsJsonPrimitive("oidcJwksUrl").getAsString(),
              Option(js.getAsJsonObject("signingKey")).map({ new Kid2Pem(_) }),
              js.getAsJsonArray("verifyKeys").asScala.map({ jsIt => new Kid2Pem(jsIt.getAsJsonObject()) }).toSet
            )
        }

        override def get():Config = singleton
    }
    
    class Provider @inject.Inject() (
        helper:KeyHelper, 
        config:Config,
        @inject.name.Named("little.cloudmgr.domain") cloud:String,
        sessionFactory:inject.Provider[Session.Builder]
    ) extends inject.Provider[LocalKeySessionMgr] {
        lazy val singleton:LocalKeySessionMgr = {
            val signingKey = config.signingKey.map({ kid2pem => helper.loadPrivateKey(kid2pem.kid, kid2pem.pem) })
            val sessionKeys = config.verifyKeys.map({ kid2pem => helper.loadPublicKey(kid2pem.kid, kid2pem.pem) }).toSet
            val oidcKeys = helper.loadJwksKeys(new java.net.URL(config.oidcJwksUrl))
            new LocalKeySessionMgr(signingKey, sessionKeys, oidcKeys, cloud, sessionFactory)
        }

        def get():LocalKeySessionMgr = singleton
    }
}
