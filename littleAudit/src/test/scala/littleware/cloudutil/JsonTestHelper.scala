package littleware.cloudutil

import com.google.gson
import littleware.scala.PropertyBuilder
import org.junit.Assert._


object JsonTestHelper {
    def testSerialize[T](
        thing:T, builder:PropertyBuilder[T], gs:gson.Gson
        ) = {
        val thing2 = builder.copy(thing).build()
        assertEquals("copy works", thing, thing2)
        val json = gs.toJson(thing)
        val json2 = gs.toJson(thing2)
        assertEquals("copy generates same json", json, json2)
        val thing3 = gs.fromJson(json2, thing.getClass())
        assertEquals("json deserialize looks ok", thing, thing3)
        val json3 = gs.toJson(thing3)
        assertEquals("json double serialize looks ok", json, json3)
    }
}