package littleware.cloudmgr.service.lambda

import com.google.inject

import littleware.cloudmgr.service
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(classOf[littleware.test.LittleTestRunner])
class SessionHandlerTester @inject.Inject() (handler: SessionHandler) extends littleware.scala.test.LittleTest {
    
    @Test
    def testInjection() = try {
        assertTrue(s"this test has been implemented", false)
    } catch basicHandler

}
