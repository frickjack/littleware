package littleware.audit

import org.apache.avro.Schema
import org.apache.avro.generic.GenericRecord


trait AvroPickler[T] {
    def schema: Schema
    def toAvroGeneric(thing:T): GenericRecord
    def fromAvroGeneric(generic:GenericRecord): T
}

object AvroPickler {
    def genericToBytes(record:GenericRecord):Array[byte] = {
        
    }
}

