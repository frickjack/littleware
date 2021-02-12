package littleware.audit

import org.apache.avro.generic.{ GenericRecordBuilder, GenericRecord }
import org.apache.avro.Schema
import scala.util.{ Try, Success, Failure, Using }

case class TestMess(name:String, favoriteNumber:Int = 3, favoriteColor: String = "blue") {}

object TestMess extends AvroPickler[TestMess] {
    lazy val schema:Schema = {
        val schemaPath = "littleware/audit/testmess.avsc"
        Using(TestMess.getClass().getClassLoader().getResourceAsStream(schemaPath)) {
            istream => new Schema.Parser().parse(istream)
        } match {
            case Success(schema) => schema
            case Failure(ex) => throw new IllegalStateException("failed to load schema", ex)
        }
    }

    def toAvroGeneric(mess:TestMess): GenericRecord = 
        new GenericRecordBuilder(schema).set("name", mess.name
            ).set("favorite_number", mess.favoriteNumber
            ).set("favorite_color", mess.favoriteColor
            ).build()

    def fromAvroGeneric(generic:GenericRecord) :TestMess =
        TestMess(
                generic.get("name").toString(),
                generic.get("favorite_number").asInstanceOf[Int],
                generic.get("favorite_color").toString()
            )
}
