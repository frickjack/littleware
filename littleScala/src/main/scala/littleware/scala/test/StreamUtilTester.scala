/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.scala.test

import java.io
import java.util.logging.Level
import junit.framework.Assert
import junit.framework.TestCase
import littleware.scala.{LazyLogger,StreamUtil}

class StreamUtilTester extends TestCase( "testStreamUtil" ) {
  val log = LazyLogger( getClass )

  def testStreamUtil():Unit = try {
    val testString = ((new StringBuilder) /: (0 until 100))(
      (sb,number) => sb.append( number ).append( ". " ).append( "Bla bla bla bla" ).append( "\n" )
    ).toString
    Assert.assertTrue( "readLineSream found 100 lines",
                      100 == StreamUtil.readLineStream( new io.BufferedReader( new io.StringReader( testString )) ).size
    )
    val sWriter = new io.StringWriter
    StreamUtil.in2Out( (new io.StringReader( testString )).read, sWriter.write, new Array[Char](5) )
    Assert.assertTrue( "in2Out preserves content", sWriter.toString == testString )
    val testFile1 = io.File.createTempFile( ".txt",  "StreamUtilTester" )
    val testFile2 = io.File.createTempFile( ".txt",  "StreamUtilTester" )
    val file1Writer = new io.OutputStreamWriter( new io.FileOutputStream( testFile1 ), littleware.base.Whatever.UTF8 )
    file1Writer.write( testString )
    file1Writer.close
    StreamUtil.copy( testFile1, testFile2 )
    val s1 = StreamUtil.readAll( testFile1 )
    Assert.assertTrue( "readAll read our test string", testString == s1 )
    Assert.assertTrue( "file copy is consistent", s1 == StreamUtil.readAll( testFile2 ) )
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Test failed", ex )
        Assert.fail( "Caught exception: " + ex )
    }
  }
}
