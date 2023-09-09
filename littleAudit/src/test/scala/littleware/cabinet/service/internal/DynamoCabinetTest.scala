package littleware.cabinet.service.internal

import com.google.inject

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

import java.util.Date
import java.util.UUID

import scala.util.{Failure, Success}


@ExtendWith(Array(classOf[littleware.test.LittleParameterResolver]))
class DynamoCabinetTest @inject.Inject() (
    dynamoCab: DynamoCabinet
) {
    
    @Test
    def testDynamoPutGet() = {
        val fullThing = DynamoCabinet.DynamoThing(
                UUID.randomUUID().toString(),
                UUID.randomUUID().toString(),
                "metadata",
                Option("details"),
                Option("payload"),
                1L,
                Math.floorDiv(new Date().getTime(), 1000) + 300
                )
        dynamoCab.putThing(fullThing).toCompletableFuture().get() match {
            case Success(resultThing) => assertSame(fullThing, resultThing)
            case Failure(err) => fail("putThing failed", err)
        }
        dynamoCab.getThing(fullThing.pk, fullThing.sk, true, true).toCompletableFuture().get() match {
            case Success(optThing) => {
                assertTrue(optThing.isDefined)
                assertEquals(fullThing, optThing.get)
            }
            case Failure(err) => {
                fail("getThing fails", err)
            }
        }

        val partialThing = DynamoCabinet.DynamoThing.builder(
            ).copy(fullThing
            ).details(None).payload(None).newVersion(2L
            ).build()
        dynamoCab.updateThing(partialThing).toCompletableFuture().get() match {
            case Success(resultThing) => assertSame(partialThing, resultThing)
            case Failure(err) => fail("updateThing failed", err)
        }
        val expectedThing = DynamoCabinet.DynamoThing.builder().copy(partialThing
            ).details(fullThing.details
            ).payload(fullThing.payload
            ).build()
        dynamoCab.getThing(partialThing.pk, partialThing.sk, true, true).toCompletableFuture().get() match {
            case Success(optThing) => {
                assertTrue(optThing.isDefined)
                assertEquals(expectedThing, optThing.get)
            }
            case Failure(err) => {
                fail("getThing fails", err)
            }
        }
    }
}
