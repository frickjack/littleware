package littleware.audit

import java.util.logging.Level
import org.junit.Assert._
import org.junit.Test
import littleware.test.LittleTest.log

/**
 * Little tester of GetoptHelper
 */
class AvroTester extends littleware.scala.test.LittleTest {
    
  @Test
  def testMess():Unit = try {
      val mess = TestMess("frickjack")
      val avroGeneric = TestMess.toAvroGeneric(mess)
      assertTrue(s"built an avro generic: ${mess.name} ?= ${avroGeneric.get("name")}", mess.name == avroGeneric.get("name"))
  } catch basicHandler 
}
