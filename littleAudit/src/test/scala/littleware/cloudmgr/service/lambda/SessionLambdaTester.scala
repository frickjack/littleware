package littleware.cloudmgr.service.lambda

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.google.gson
import com.google.inject

import littleware.cloudmgr.service
import littleware.cloudutil.LRN
import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(classOf[littleware.test.LittleTestRunner])
class SessionLambdaTester @inject.Inject() (
  lambda:SessionLambda,
  gs: gson.Gson
  ) extends littleware.scala.test.LittleTest {
    
    @Test
    def testOptions() = try {
        val request = new APIGatewayProxyRequestEvent().withHttpMethod("OPTIONS"
          ).withPath("/session/" + LRN.zeroId
          )
        val response = lambda.handleRequest(request, null)
        assertEquals("OPTIONS gets 200", 200, response.getStatusCode())
    } catch basicHandler

    @Test
    def testVersionInfo() = try {
        val request = new APIGatewayProxyRequestEvent().withHttpMethod("GET"
          ).withPath("/version"
          )
        val response = lambda.handleRequest(request, null)

        assertEquals("/version gets 200", 200, response.getStatusCode())
        val semver = gs.fromJson(response.getBody(),
          classOf[gson.JsonObject]
          ).getAsJsonPrimitive("semver").getAsString()
        assertTrue("/version has json body", !semver.isEmpty())
    } catch basicHandler
}
