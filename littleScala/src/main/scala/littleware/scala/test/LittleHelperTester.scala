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

import java.util.logging.Level
import junit.framework.Assert
import junit.framework.TestCase
import littleware.scala.LazyLogger
import littleware.scala.LittleHelper._

class LittleHelperTester extends TestCase( "testLittleHelper" ) {
  val log = LazyLogger( getClass )

  def testLittleHelper():Unit = try {
    Assert.assertTrue( "Empty check handled string of spaces", ! emptyCheck( "    " ).isDefined )
    Assert.assertTrue( "ToJavaList has right size", 3 == toJavaList( List( "A", "B", "C" ) ).size )
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Test failed", ex )
        Assert.fail( "Caught exception: " + ex )
    }
  }
}
