package littleware.cloudmgr.service.lambda


import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

import com.google.gson

import littleware.cloudmgr.service
import littleware.cloudutil.{ LRN, Session }

import scala.jdk.CollectionConverters._

/**
 * Adapted from https://github.com/awsdocs/aws-lambda-developer-guide/blob/main/sample-apps/java-events/src/main/java/example/HandlerApiGateway.java
 */
class SessionHandler extends RequestHandler[APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent]{
  val gs = new gson.GsonBuilder().setPrettyPrinting().create()

  override def handleRequest(event:APIGatewayProxyRequestEvent, context:Context):APIGatewayProxyResponseEvent = {
    val logger = context.getLogger()
    val response = new APIGatewayProxyResponseEvent()
    response.setIsBase64Encoded(false)
    response.setStatusCode(200)

    val headers = Map(
        "Content-Type" -> "application/json"
        )
    response.setHeaders(headers.asJava)
    response.setBody(s"""
    { "message": "hello from lambda" }
    """
    )
    // log execution details
    SessionHandler.logEnvironment(event, context, gs)
    response
  }
}

object SessionHandler {
    def logEnvironment(event:AnyRef, context:Context, gs:gson.Gson):Unit = {
        val logger = context.getLogger()
        // log execution details
        logger.log("ENVIRONMENT VARIABLES: " + gs.toJson(System.getenv()))
        logger.log("CONTEXT: " + gs.toJson(context))
        // log event details
        logger.log("EVENT: " + gs.toJson(event))
        logger.log("EVENT TYPE: " + event.getClass().toString())
    }
}
