package littleware.cloudmgr.service.internal

import com.google.gson
import com.google.inject

import java.security.interfaces.{ ECPublicKey, ECPrivateKey, RSAPublicKey }
import java.security.spec._

import littleware.cloudmgr.service.SessionMgr
import scala.jdk.CollectionConverters._


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

    /**
     * Load EC X509 pem key from environment variable with name LITTLE_CLOUD_PUBKEY_$kid
     */
    def loadPublicKeyFromEnv(kid:String, env:Map[String, String]):SessionMgr.PublicKeyInfo = {
      // Get the path to the key pair from the environment
      val envKey = s"LITTLE_AUDIT_PUBKEY_${kid}"
      val pemStr = env.get(envKey) getOrElse { throw new IllegalStateException("environment not set: " + envKey) }
      loadPublicKey(kid, pemStr)
    }

    def loadPublicKey(kid:String, pemStr:String):SessionMgr.PublicKeyInfo = {
      val key = ecKeyFactory.generatePublic(decodePem(pemStr))
      SessionMgr.PublicKeyInfo(kid, "ES256", key)
    }

    def loadPublicKey(kid:String, bytes:Array[Byte]):SessionMgr.PublicKeyInfo = {
      val key = ecKeyFactory.generatePublic(bytes)
      SessionMgr.PublicKeyInfo(kid, "ES256", key)
    }

    /**
     * Load EC X509 pem key from environment variable with name LITTLE_CLOUD_PUBKEY_$kid
     */
    def loadPrivateKeyFromEnv(kid:String, env:Map[String, String]):SessionMgr.PrivateKeyInfo = {
      // Get the path to the key pair from the environment
      val envKey = "LITTLE_AUDIT_PRIVKEY_${kid}"
      val pemStr = env.get(envKey) getOrElse { throw new IllegalStateException("environment not set: " + envKey) }
      loadPrivateKey(kid, pemStr)
    }

    def loadPrivateKey(kid:String, pemStr:String):SessionMgr.PrivateKeyInfo = {
      val key = ecKeyFactory.generatePrivate(decodePem(pemStr))
      SessionMgr.PrivateKeyInfo(kid, "ES256", key)
    }

    /**
     * Load jwks keys from a URL like: https://www.googleapis.com/oauth2/v3/certs
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
          (json:gson.JsonElement) => { 
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

        def generatePublic(base64: String):ECPublicKey = {
            val bytes = b64Decoder.decode(base64.getBytes(utf8))
            generatePublic(bytes)
        }

        def generatePublic(bytes: Array[Byte]):ECPublicKey = {
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
