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
class LocalKeySessionMgrTester @inject.Inject() (mgr:LocalKeySessionMgr) extends littleware.scala.test.LittleTest {
    
    @Test
    def testListKeys() = try {
        val keys = mgr.publicKeys()
        assertTrue("got a single public key", !keys.isEmpty)
    } catch basicHandler
}
