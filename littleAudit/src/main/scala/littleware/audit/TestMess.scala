package littleware.audit

import org.apache.avro.generic.{ GenericData, GenericRecord }
import org.apache.avro.Schema
import scala.util.{ Try, Success, Failure, Using }

case class TestMess(name:String, favoriteNumber:Int = 3, favoriteColor: String = "blue") {}

object TestMess {
    lazy val trySchema:Try[Schema] = {
        val schemaPath = "littleware/audit/testmess.avsc"
        Using(TestMess.getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            istream => new Schema.Parser().parse(istream)
        }
    }

    def toAvroGeneric(mess:TestMess): GenericRecord = {
        trySchema match {
            case Success(schema) => val user1:GenericRecord = new GenericData.Record(schema)
                user1.put("name", mess.name)
                user1.put("favorite_number", mess.favoriteNumber)
                user1.put("favorite_color", mess.favoriteColor)
                user1
            case Failure(ex) => throw new IllegalStateException("failed to load schema", ex)
        }
    }
}
