package littleware.scala.test

import littleware.test.LittleTest

/**
 * Little specialization of littleware.test.LittleTest with
 * some scala friendliness
 */
class LittleTest {
  val basicHandler:PartialFunction[Throwable,Unit] = {
    case ex:Exception => LittleTest.handle(ex)
    case ex:java.lang.AssertionError => LittleTest.handle(ex)
    case ex => throw ex
  }
}
