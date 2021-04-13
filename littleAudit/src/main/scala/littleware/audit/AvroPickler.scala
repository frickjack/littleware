package littleware.audit

import org.apache.avro.Schema
import org.apache.avro.generic._
import org.apache.avro.io._
import scala.util.Using


trait AvroPickler[T] {
    def schema: Schema
    def toAvroGeneric(thing:T): GenericRecord
    def fromAvroGeneric(generic:GenericRecord): T
}

object AvroPickler {
    def genericToBytes(record:GenericRecord):Array[Byte] = {
        val writer:DatumWriter[GenericRecord] = new GenericDatumWriter[GenericRecord](record.getSchema())
        Using.resource(new java.io.ByteArrayOutputStream()) {
            out => 
            val encoder:BinaryEncoder = EncoderFactory.get().binaryEncoder(out, null)
            writer.write(record, encoder)
            encoder.flush()
            out.toByteArray()
        }
    }

    def bytesToGeneric(bytes:Array[Byte], schema:Schema):GenericRecord = {
        val reader:DatumReader[GenericRecord] = new GenericDatumReader[GenericRecord](schema)
        val decoder:BinaryDecoder = DecoderFactory.get().binaryDecoder(bytes, null)
        reader.read(null, decoder)
    }

}

