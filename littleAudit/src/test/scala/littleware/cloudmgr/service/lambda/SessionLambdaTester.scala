package littleware.cloudmgr.service.lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent

import littleware.cloudmgr.service
import littleware.cloudutil.LRN
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


class SessionLambdaTester extends littleware.scala.test.LittleTest {
    val lambda = new SessionLambda()

    @Test
    def testOptions() = try {
        val request = new APIGatewayProxyRequestEvent().withHttpMethod("OPTIONS"
          ).withPath("/session/" + LRN.zeroId
          )
        val response = lambda.handleRequest(request, null)
        assertEquals("OPTIONS gets 200", 200, response.getStatusCode())
    } catch basicHandler

}
