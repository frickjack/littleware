/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.scala
package test


import junit.framework.Assert._
import java.util.logging.{Level,Logger}

class PropertyBuilderTester extends LittleTest {
  setName( "testPropBuilder" )
  
  def testPropBuilder():Unit = try {
    val builder = new PropertyBuilderTester.TestBuilder();
    {
      val sanity = builder.checkSanity()
      assertTrue( "test builder starts in invalid state: " + sanity.mkString( ", " ), sanity.nonEmpty )
    }
    builder.aString( "bla" ).iNumber( 5 ).bBuffer
  } catch basicHandler
}

object PropertyBuilderTester {
  class TestBuilder extends PropertyBuilder {
    val aString = new NotNullProperty[String]().name( "aString" )
    val iNumber = new IntProperty().name( "iNumber" )
    val bBuffer = new BufferProperty[String]().name( "bBuffer" )
  }
}