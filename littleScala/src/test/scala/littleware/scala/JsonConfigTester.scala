package littleware.scala

import com.google.gson
import java.util.logging.Level
import org.junit.Assert._
import org.junit.Test
import littleware.test.LittleTest.log

/**
 * Little tester of GetoptHelper
 */
class JsonConfigTester extends test.LittleTest {
  def toJson(map:Map[String, String]):gson.JsonObject = 
    map.toSeq.foldLeft(
        new gson.JsonObject()
      )((js, it) => { js.addProperty(it._1, it._2); js })
  
  @Test
  def testJsonMerge():Unit = try {
    val input = Seq(
      Map("a" -> "A"),
      Map("b" -> "B"),
      Map("c" -> "C"),
      Map("a" -> "a", "b" -> "b")
    ).map({ toJson _ })
    val expected = Some(toJson(Map("a" -> "a", "b" -> "b", "c" -> "C")))
    val result = JsonConfigLoader.jsonMerge(input)
    assertEquals("json merge works", expected, result)
  } catch basicHandler 


  @Test
  def testConfigLoad():Unit = {
    System.setProperty("littleware_scala_LITTLE_SCALA_CONFIGTEST.json", """{ "c": "C" }""")
    val expected = Some(toJson(Map("LITTLE_SCALA_A" -> "a", "LITTLE_SCALA_B" -> "B", "c" -> "C")))
    val result = JsonConfigLoader.loadConfig("littleware/scala/LITTLE_SCALA_CONFIGTEST", getClass().getClassLoader())
    assertEquals("json config load works", expected, result)
  }

  @Test
  def testConfigBinder():Unit = {
    System.setProperty("littleware_scala_LITTLE_SCALA_CONFIGTEST.json", "{}")
    val obj = JsonConfigLoader.loadConfig("littleware/scala/LITTLE_SCALA_CONFIGTEST", getClass().getClassLoader()).get
    val expected = Set("LITTLE_SCALA_A", "LITTLE_SCALA_B")
    assertEquals("json binding looks ok", expected, JsonConfigLoader.bindKeys(null, obj))
  }

}
