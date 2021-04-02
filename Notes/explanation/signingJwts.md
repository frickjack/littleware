# TL;DR

How to setup keys and code to sign and verify JWTS with ES256.

## Problem and Audience

A developer of a system that uses json web tokens (JWT) to authenticate HTTP API requests needs to generate asymmetric cryptographic keys, load the keys into code, then use the keys to sign and validate tokens.

We are building a multi-tenant system that implements a hierarchy where each tenant (project) may enable one or more api's.  An end user authenticates with the global system (OIDC authentication client) via a handshake with a [Cognito](https://aws.amazon.com/cognito/) identity provider, then acquires a short lived session token to interact with 
a particular api under a particular project (OIDC resource server). 

It would be nice to
simply implement the session token as a Cognito OIDC access token, 
but our system has a few requirements that push us to manage
our own session tokens for now.  First, each (project, api) pair is effectively an [OIDC resource server](https://docs.microsoft.com/en-us/azure/active-directory/develop/active-directory-v2-protocols#the-basics) in our model, and projects and api's are created dynamically, so managing the custom JWT claims with [Cognito resource servers](https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-define-resource-servers.html) would be messy.

Second, we want to be able to support robot accounts at the project level, and a Cognito mechanism to easily provision robot accounts and tokens is not obvious to us.  So we decided to
manage our "session" tokens in the application, and rely on Cognito
to federate identity providers for user authentication.

## JWTS with ES256

I know very little about cryptography, authentication, and authorization; but fortunately people that know
more than me share their knowledge online.  [Scott Brady's bLog](https://www.scottbrady91.com/JOSE/JWTs-Which-Signing-Algorithm-Should-I-Use)
gives a nice overview of JWT signing.  We want to sign and verify JWTs in scala using the elliptic curve ES256 algorithm - which improves on RSA256 in a few ways, and is widely supported.


There are different ways to generate an elliptic curve sha-256 key pair, but EC keys saved to pem files are supported by multiple tools, and are easy to save to configuration stores like AWS [SSM parameter store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html) or [secrets manager](https://aws.amazon.com/secrets-manager/).

This bash function uses openssl to generate keys in pem files.

```
#
# Bash function to generate new ES256 key pair
#
newkey() {
    local kid=${1:-$(date +%Y%m)}
    local secretsFolder=$HOME/Secrets/littleAudit
    
    (
        mkdir -p "$secretsFolder"
        cd "$secretsFolder" || return 1
        if [[ ! -f ec256-key-${kid}.pem ]]; then
          openssl ecparam -genkey -name prime256v1 -noout -out ec256-key-${kid}.pem
        fi
        # convert the key to pkcs8 format
        openssl pkcs8 -topk8 -nocrypt -in ec256-key-${kid}.pem -out ec256-pkcs8-key-${kid}.pem
        # extract the public key
        openssl ec -in ec256-pkcs8-key-${kid}.pem -pubout -out ec256-pubkey-${kid}.pem
    )
}

```

### Load the keys into code

Now that we have our keys - we need to load them into our scala application.

```
class KeyHelper @inject.Inject() (
  gs: gson.Gson, 
  ecKeyFactory:KeyHelper.EcKeyFactory, 
  rsaKeyFactory:KeyHelper.RsaKeyFactory
  ) {    
    /**
     * @return pem input with pem file prefix/suffix and empty space removed
     */
    def decodePem(pem:String): String = {
      pem.replaceAll(raw"-----[\w ]+-----", "").replaceAll("\\s+", "")
    }

    
    def loadPublicKey(kid:String, pemStr:String):SessionMgr.PublicKeyInfo = {
      val key = ecKeyFactory.generatePublic(decodePem(pemStr))
      SessionMgr.PublicKeyInfo(kid, "ES256", key)
    }


    def loadPrivateKey(kid:String, pemStr:String):SessionMgr.PrivateKeyInfo = {
      val key = ecKeyFactory.generatePrivate(decodePem(pemStr))
      SessionMgr.PrivateKeyInfo(kid, "ES256", key)
    }

    /**
     * Load keys from a jwks url like 
     *    https://www.googleapis.com/oauth2/v3/certs
     */
    def loadJwksKeys(jwksUrl:java.net.URL): Set[SessionMgr.PublicKeyInfo] = {
      val jwksStr = {
        val connection = jwksUrl.openConnection()
        connection.setRequestProperty("Accept-Charset", KeyHelper.utf8)
        connection.setRequestProperty("Accept", "application/json")
        val response = new java.io.BufferedReader(new java.io.InputStreamReader(connection.getInputStream(), KeyHelper.utf8))
        try {
            littleware.base.Whatever.get().readAll(response)
        } finally {
            response.close()
        }
      }

      gs.fromJson(jwksStr, classOf[gson.JsonObject]).getAsJsonArray("keys").asScala.map(
          { 
            json:gson.JsonElement =>
            val jsKeyInfo = json.getAsJsonObject()
            val kid = jsKeyInfo.getAsJsonPrimitive("kid").getAsString()
            val n = jsKeyInfo.getAsJsonPrimitive("n").getAsString()
            val e = jsKeyInfo.getAsJsonPrimitive("e").getAsString()
            val pubKey = rsaKeyFactory.generatePublic(n, e)
            SessionMgr.PublicKeyInfo(kid, "RSA256", pubKey)
          }
      ).toSet 
    }
}

object KeyHelper {
    val utf8 = "UTF-8"

    /**
     * Little injectable key factory hard wired to use X509 key spec for public key
     */
    class EcKeyFactory {
        val keyFactory = java.security.KeyFactory.getInstance("EC")
        val b64Decoder = java.util.Base64.getDecoder()

        def generatePublic(base64:String):ECPublicKey = {
            val bytes = b64Decoder.decode(base64.getBytes(utf8))
            val spec = new X509EncodedKeySpec(bytes)
      
            keyFactory.generatePublic(spec).asInstanceOf[ECPublicKey]
        }
        
        def generatePrivate(base64:String):ECPrivateKey = {
            val bytes = b64Decoder.decode(base64.getBytes(utf8))
            val spec = new PKCS8EncodedKeySpec(bytes)
      
            keyFactory.generatePrivate(spec).asInstanceOf[ECPrivateKey]
       }
    }

    /**
     * Little injectable key factory hard wired for RSA jwks decoding
     * See: https://github.com/auth0/jwks-rsa-java/blob/master/src/main/java/com/auth0/jwk/Jwk.java
     */
    class RsaKeyFactory {
        private val keyFactory = java.security.KeyFactory.getInstance("RSA")
        private val b64Decoder = java.util.Base64.getUrlDecoder()

        def generatePublic(n:String, e:String):RSAPublicKey = {
            val modulus = new java.math.BigInteger(1, b64Decoder.decode(n))
            val exponent = new java.math.BigInteger(1, b64Decoder.decode(e))
            keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent)).asInstanceOf[RSAPublicKey]
        }
    }
}

```

### Sign and verify JWTs

Now that we have loaded our keys, we can use them to sign and verify JWTs.
[Okta](https://www.okta.com/) has published open source [code for working with JWTs](https://github.com/jwtk/jjwt#java-jwt-json-web-token-for-java-and-android) , [Auth0](https://auth0.com/) has published open source [code for working with JWK](https://github.com/auth0/jwks-rsa-java), and [AWS KMS](https://aws.amazon.com/kms/) supports elliptic curve digital signing algorithms with [asymmetric keys](https://docs.aws.amazon.com/kms/latest/developerguide/symm-asymm-concepts.html#asymmetric-cmks).

```
import com.google.inject
// see https://github.com/jwtk/jjwt#java-jwt-json-web-token-for-java-and-android
import io.{jsonwebtoken => jwt}
import java.security.{ Key, PublicKey }
import java.util.UUID
import scala.util.Try

import littleware.cloudmgr.service.SessionMgr
import littleware.cloudmgr.service.SessionMgr.InvalidTokenException
import littleware.cloudmgr.service.littleModule
import littleware.cloudutil.{ LRN, Session }

/**
 * @param signingKey for signing new session tokens
 * @param verifyKeys for verifying the signature of session tokens
 */
@inject.ProvidedBy(classOf[LocalKeySessionMgr.Provider])
@inject.Singleton()
class LocalKeySessionMgr (
    signingKey: Option[SessionMgr.PrivateKeyInfo],
    sessionKeys: Set[SessionMgr.PublicKeyInfo],
    oidcKeys: Set[SessionMgr.PublicKeyInfo],
    issuer:String,
    sessionFactory:inject.Provider[Session.Builder]
    ) extends SessionMgr {

    val resolver = new jwt.SigningKeyResolverAdapter() {
        override def resolveSigningKey(jwsHeader:jwt.JwsHeader[T] forSome { type T <: jwt.JwsHeader[T] }, claims:jwt.Claims):java.security.Key = {
            val kid = jwsHeader.getKeyId()
            (
                {
                    if (claims.getIssuer() == issuer) {
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

    ...

    def jwsToClaims(jwsIdToken:String):Try[jwt.Claims] = Try(
        { 
            jwt.Jwts.parserBuilder(
            ).setSigningKeyResolver(resolver
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
    ).flatMap(
        claims => Try(
            {
                if (claims.getExpiration().before(new java.util.Date())) {
                    throw new InvalidTokenException(s"auth token expired: ${claims.getExpiration()}")
                }
                claims
            }
        )
    )


    def sessionToJws(session:Session):String = {
        val signingInfo = signingKey getOrElse { throw new UnsupportedOperationException("signing key not available") }
        jwt.Jwts.builder(
        ).setHeaderParam(jwt.JwsHeader.KEY_ID, signingInfo.kid
        ).setClaims(SessionMgr.sessionToClaims(session)
        ).signWith(signingInfo.privKey
        ).compact()
    }

    def jwsToSession(jws:String):Try[Session] = jwsToClaims(jws
        ) map { claims => SessionMgr.claimsToSession(claims) }

    ...
}


```

This code is all online under this [github repo](https://github.com/frickjack/littleware/tree/dev), but is in a state of flux.


## Summary

To sign and verify JWTs we need to generate keys, load the keys into code, and use the keys to sign and verify tokens.  We plan to add support for token signing with AWS KMS soon.
