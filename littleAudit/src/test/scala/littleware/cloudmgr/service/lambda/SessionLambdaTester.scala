package littleware.cloudmgr.service.lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.google.inject

import littleware.cloudmgr.service
import littleware.cloudutil.LRN
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(classOf[littleware.test.LittleTestRunner])
class SessionLambdaTester @inject.Inject() (lambda:SessionLambda) extends littleware.scala.test.LittleTest {
    
    @Test
    def testOptions() = try {
        val request = new APIGatewayProxyRequestEvent().withHttpMethod("OPTIONS"
          ).withPath("/session/" + LRN.zeroId
          )
        val response = lambda.handleRequest(request, null)
        assertEquals("OPTIONS gets 200", 200, response.getStatusCode())
    } catch basicHandler

}
