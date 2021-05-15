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
class AwsSessionMgrTester @inject.Inject() (
    mgr:AwsSessionMgr,
    sessionFactory:inject.Provider[Session.Builder]
    ) extends LocalKeySessionMgrTester(mgr, sessionFactory) {
}
