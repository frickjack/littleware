package littleware.cloudmgr.service.internal

import com.google.inject

import littleware.cloudmgr.service
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Caller should set LITTLE_AUDIT_PUBKEY_testkey and LITTLE_AUDIT_PRIVKEY_testkey
 */
@RunWith(classOf[littleware.test.LittleTestRunner])
class KeyHelperTester @inject.Inject() (helper:KeyHelper) extends littleware.scala.test.LittleTest {
    val kid = "testkey"
    val jwksUrl = new java.net.URL("https://www.googleapis.com/oauth2/v3/certs")
    val publicKeyPem = s"""
-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE1nXiKMBJieZcaZu4BPBrbwZwdRQU
yJXGR3Q9RTzPdKHhAK2hhrZ5bmGBHT4iT969vZ96hXBk4L6gwBOPW3OKJg==
-----END PUBLIC KEY-----
"""
    val privateKeyPem = s"""
-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgs02I2exqJsdAoHef
54/cjmlRvww903MKp0AOPqlRRXqhRANCAATWdeIowEmJ5lxpm7gE8GtvBnB1FBTI
lcZHdD1FPM90oeEAraGGtnluYYEdPiJP3r29n3qFcGTgvqDAE49bc4om
-----END PRIVATE KEY-----
"""

    @Test
    def testLoadPubKey() = try {
        val kinfo = helper.loadPublicKey(kid, publicKeyPem)
        assertTrue(s"got expected kid: ${kinfo.kid} =? ${kid}", kinfo.kid == kid)
        assertTrue(s"got expected alg: ${kinfo.alg} =? ES256", kinfo.alg == "ES256")
    } catch basicHandler

    @Test
    def testLoadPrivKey() = try {
        val kinfo = helper.loadPrivateKey(kid, privateKeyPem)
        assertTrue(s"got expected kid: ${kinfo.kid} =? ${kid}", kinfo.kid == kid)
        assertTrue(s"got expected alg: ${kinfo.alg} =? ES256", kinfo.alg == "ES256")
    } catch basicHandler

    @Test
    def testLoadJwks() = try {
        val keys = helper.loadJwksKeys(jwksUrl)
        assertTrue("loadJwks got some keys from ${jwksUrl}", !keys.isEmpty)
    } catch basicHandler
}
