package littleware.cloudmgr.service.internal

import com.google.inject
import java.util.UUID
import java.util.logging
import littleware.cloudmgr.service
import littleware.cloudutil.{ LRN, Session }
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(classOf[littleware.test.LittleTestRunner])
class LocalKeySessionMgrTester @inject.Inject() (
    mgr:service.SessionMgr,
    sessionFactory:inject.Provider[Session.Builder]
    ) extends littleware.scala.test.LittleTest {
    val log = logging.Logger.getLogger(this.getClass().getName())

    @Test
    def testListKeys() = try {
        val keys = mgr.publicKeys()
        assertTrue("got a single public key", !keys.isEmpty)
    } catch basicHandler

    @Test
    def testSignVerify() = try {
        val session = sessionFactory.get(
        ).projectId(LRN.zeroId
        ).api("little-test"
        ).id(UUID.randomUUID()
        ).cellId(LRN.zeroId
        ).subject("frickjack@frickjack.com"
        ).build()
        val jws = mgr.sessionToJws(session)
        log.info(s"session to jws got: ${jws}")
        val session2 = mgr.jwsToSession(jws).get
        assertTrue("session to jws to session is consistent", session == session2)
    } catch basicHandler

}
