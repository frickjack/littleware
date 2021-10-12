package littleware.cloudutil

import com.google.gson
import com.google.inject

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith( classOf[littleware.test.LittleTestRunner] )
class LRNTester @inject.Inject() (
    builderProvider: inject.Provider[LRN.LRPathBuilder],
    gsonProvider: inject.Provider[gson.Gson]
) extends littleware.scala.test.LittleTest {

    def buildTestLrn():LRPath = builderProvider.get(
        ).cloud("test.cloud"
        ).api("testapi"
        ).drawer("testdrawer"
        ).projectId(LRN.zeroId
        ).resourceType("testrt"
        ).path("*").build()

    @Test
    def testLRNBuilder() = try {
        val lrn = buildTestLrn()
        assertTrue(s"api equal: ${lrn.api} ?= testapi", lrn.api == "testapi")
    } catch basicHandler

    @Test
    def testJson():Unit = {
        JsonTestHelper.testSerialize(buildTestLrn(), builderProvider.get(), gsonProvider.get())
    }

}
