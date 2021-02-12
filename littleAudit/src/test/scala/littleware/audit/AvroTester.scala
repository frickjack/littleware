package littleware.audit

import java.util.logging.Level
import org.apache.avro.io._
import org.junit.Assert._
import org.junit.Test
import org.apache.avro.generic.{ GenericDatumReader, GenericDatumWriter, GenericRecord }
import littleware.test.LittleTest.log
import scala.util.Using

/**
 * Little tester of GetoptHelper
 */
class AvroTester extends littleware.scala.test.LittleTest {
    
    @Test
    def testMessPickler():Unit = try {
        val mess = TestMess("frickjack")
        val avroGeneric = TestMess.toAvroGeneric(mess)
        assertTrue(s"built an avro generic: ${mess.name} ?= ${avroGeneric.get("name")}", mess.name == avroGeneric.get("name"))
        val mess2 = TestMess.fromAvroGeneric(avroGeneric)
        assertTrue("pickle/unpickle is consistent", mess == mess2)
    } catch basicHandler

    /**
    * See the avro quick start at:
    *   http://avro.apache.org/docs/current/gettingstartedjava.html
    */
    @Test
    def testMessSerialization():Unit = try {
        val mess = TestMess("frickjack")

        val reader:DatumReader[GenericRecord] = new GenericDatumReader[GenericRecord](TestMess.schema)
        val decoder:BinaryDecoder = DecoderFactory.get().binaryDecoder(bytes, null)
        val mess2 = TestMess.fromAvroGeneric(reader.read(null, decoder))
        assertTrue(s"serialize/deserialize is consistent ${mess} ?= ${mess2}", mess == mess2)
    } catch basicHandler
}
