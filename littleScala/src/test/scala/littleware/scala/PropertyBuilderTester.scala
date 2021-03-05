package littleware.scala


import org.junit.Assert._
import org.junit.Test

import PropertyBuilder.{ rxValidator, notNullValidator, positiveIntValidator }

class PropertyBuilderTester extends test.LittleTest {
  
  @Test
  def testPropBuilder():Unit = try {
    val builder = new PropertyBuilderTester.TestBuilder();
    {
      val sanity = builder.checkSanity()
      assertTrue( "test builder starts in invalid state: " + sanity.mkString( ", " ), sanity.nonEmpty )
    }
    builder.aString( "bla" ).iNumber( 5 ).bBuffer.add("whatever").rxTest("123")
    assertTrue("rxTest fails on bad data", !builder.rxTest.checkSanity().isEmpty)
    builder.rxTest("abc123")
    assertTrue("rxTest passes on good data", builder.rxTest.checkSanity().isEmpty)
  } catch basicHandler
}

object PropertyBuilderTester {
  class TestBuilder extends PropertyBuilder[String] {
    val aString = new Property[String](null) withName "aString" withValidator notNullValidator
    val iNumber = new Property(0) withName "iNumber" withValidator positiveIntValidator
    val bBuffer = new BufferProperty[String]() withName "bBuffer"
    val rxTest = new Property("") withName "rxTest" withValidator rxValidator(raw"[a-z][0-9a-z]+".r)

    override def copy(v:String) = this
    override def build():String = "whatever"
  }
}