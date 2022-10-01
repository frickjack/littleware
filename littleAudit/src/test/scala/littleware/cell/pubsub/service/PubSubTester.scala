package littleware.cell.pubsub.service

import com.google.gson
import com.google.inject

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith( classOf[littleware.test.LittleTestRunner] )
class PubSubTester @inject.Inject() (
    mgr: PubSub
) extends littleware.scala.test.LittleTest {
    
    @Test
    def testPubSub() = try {
        assertNotNull("pubsub manager initialized ok", mgr)
    } catch basicHandler

}