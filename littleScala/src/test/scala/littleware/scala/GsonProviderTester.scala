package littleware.scala

import com.google.gson
import com.google.inject
import java.util.logging.{ Level, Logger }
import littleware.test.LittleTest.log
import org.junit.Assert._
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith


/**
 * Little tester of GetoptHelper
 */
@RunWith(classOf[littleware.test.LittleTestRunner])
class GsonProviderTester @inject.Inject() (gsProvider:GsonProvider) extends test.LittleTest {

  @Before
  def setup():Unit = {
    GsonProviderTester.init()
    //GsonProvider.registerTypeAdapter​(classOf[GsonProviderTester.TestThing], GsonProviderTester.GsonTypeAdapter)
  }

  @Test
  def testJsonWriteRead():Unit = try {
    val gs = gsProvider.get()
    val input = GsonProviderTester.TestThing("A", 55, Seq("C", "C", "C"))
    val json = gs.toJson(input, classOf[GsonProviderTester.TestThing])
    log.log(Level.INFO, "Got json: " + json)
    val output = gs.fromJson(json, classOf[GsonProviderTester.TestThing])
    assertEquals("json type adapter works", input, output)
  } catch basicHandler

}

object GsonProviderTester {
  case class TestThing (
    a: String,
    b: Long,
    c: Seq[String]
  ) {}

  object GsonTypeAdapter extends gson.TypeAdapter[TestThing] {
    override def read(reader:gson.stream.JsonReader):TestThing = {
      var a = ""
      var b = 0L
      var c:Seq[String] = Seq()

      GsonProvider.objectIterator(reader).foreach(
        {
          kv =>
          kv._1 match {
            case "a" => a = reader.nextString()
            case "b" => b = reader.nextLong()
            case "c" => c = GsonProvider.arrayIterator(reader).map({ rd => rd.nextString() }).toList
          }
        }
      )
      TestThing(a, b, c)
    }

    override def write(writer:gson.stream.JsonWriter, value:TestThing):Unit = {
      log.info("writing TestThing")
      writer.beginObject(
      ).name("a").value(value.a
      ).name("b").value(value.b
      ).name("c").beginArray()
      value.c.foreach({ v => writer.value(v) })
      writer.endArray().endObject()
    }
  }

  GsonProvider.registerTypeAdapter​(classOf[TestThing], GsonTypeAdapter)

  def init():Unit = {}
}
