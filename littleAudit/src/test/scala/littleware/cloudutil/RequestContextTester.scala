package littleware.cloudutil

import com.google.gson
import com.google.inject

import java.util.{ Date, UUID }

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(classOf[littleware.test.LittleTestRunner])
class RequestContextTester @inject.Inject() (
    cxProvider: inject.Provider[RequestContext.Builder],
    sessionProvider: inject.Provider[Session.Builder],
    gsonProvider: inject.Provider[gson.Gson]
    ) extends littleware.scala.test.LittleTest {

    def buildTestCx():RequestContext = cxProvider.get(
        ).session(
            sessionProvider.get(
            ).cellId(LRN.zeroId
            ).subject("frickjack@frickjack.com"
            ).api("testapi"
            ).projectId(LRN.zeroId
            ).build()
        ).actionPaths(
            Map(
                "littleAudit/test" -> Seq(
                    LRN.pathBuilder().cloud("test.cloud").api("testapi"
                    ).projectId(UUID.randomUUID()
                    ).resourceType("thing"
                    ).resourceGroup("resourceGroup0"
                    ).path("thing0"
                    ).build()
                )
            )
        ).build()


    @Test
    def testRequestCxBuilder() = try {
        val cx = buildTestCx()
        assertTrue(s"api equal: testapi ?= ${cx.session.api}", "testapi" == cx.session.api)
    } catch basicHandler

    @Test
    def testJson():Unit = {
        JsonTestHelper.testSerialize(buildTestCx(), cxProvider.get(), gsonProvider.get())
    }
}
