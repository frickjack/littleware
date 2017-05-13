package littleware.scala.test


/**
 * Little specialization of littleware.test.LittleTest with
 * some scala friendliness
 */
class LittleTest {
  val basicHandler:PartialFunction[Throwable,Unit] = {
    case ex:Exception => littleware.test.LittleTest.handle(ex)
    case ex:java.lang.AssertionError => littleware.test.LittleTest.handle(ex)
    case ex => throw ex
  }
}
