package littleware.cloudutil

import com.google.inject

import org.junit.Assert._
import org.junit.Test
import org.junit.runner.RunWith


@RunWith( classOf[littleware.test.LittleTestRunner] )
class LRNTester @inject.Inject() (builderProvider: inject.Provider[LRN.LRPathBuilder]) extends littleware.scala.test.LittleTest {

    @Test
    def testLRNBuilder() = try {
        val builder = builderProvider.get(
        ).cloud("test.cloud"
        ).api("testapi"
        ).drawer("testdrawer"
        ).projectId(LRN.zeroId
        ).resourceType("testrt"
        ).path("*")

        val lrn = builder.build()
        assertTrue(s"api equal: ${lrn.api} ?= ${builder.api()}", lrn.api == builder.api())
    } catch basicHandler
}
