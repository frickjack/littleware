package littleware.scala


import org.junit.Assert._
import org.junit.Test
import java.util.logging.{Level,Logger}

class PropertyBuilderTester extends test.LittleTest {
  
  @Test
  def testPropBuilder():Unit = try {
    val builder = new PropertyBuilderTester.TestBuilder();
    {
      val sanity = builder.checkSanity()
      assertTrue( "test builder starts in invalid state: " + sanity.mkString( ", " ), sanity.nonEmpty )
    }
    builder.aString( "bla" ).iNumber( 5 ).bBuffer
    import PropertyBuilder._
    assertTrue( "null check does what it does", sanityCheck( nullCheck )(null).nonEmpty )
  } catch basicHandler
}

object PropertyBuilderTester {
  class TestBuilder extends PropertyBuilder {
    val aString = new NotNullProperty[String]().name( "aString" )
    val iNumber = new IntProperty().name( "iNumber" )
    val bBuffer = new BufferProperty[String]().name( "bBuffer" )
  }
}