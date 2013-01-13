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

import com.google.inject
import java.util.concurrent.ExecutorService
import java.util.logging.Level
import junit.framework.Assert._
import junit.framework.TestCase
import littleware.base.feedback.Feedback
import littleware.scala.LittleHelper._

class LittleHelperTester @inject.Inject() ( exec:ExecutorService, fbFactory:inject.Provider[Feedback] ) extends LittleTest {
  setName( "testLittleHelper" )

  def testLittleHelper():Unit = try {
    assertTrue( "Empty check handled string of spaces", ! emptyCheck( "    " ).isDefined )
    assertTrue( "ToJavaList has right size", 3 == toJavaList( List( "A", "B", "C" ) ).size )
  } catch basicHandler
  
  def testPipeline():Unit = try {
    val pipeResult = new Pipeline( (0 until 100).toSeq ).pipeline( exec, 4, (x:Int) => x + x, (y:Int) => y.toString + "*", fbFactory.get )
    val fjResult = new Pipeline( (0 until 100).toSeq ).forkJoin( exec, 4, (x:Int) => x + x, (y:Int) => y.toString + "*", fbFactory.get )
    val check = (0 until 100).map( (x) => (x+x).toString + "*" ).toSeq
    assertTrue( "pipeline works: " + pipeResult, pipeResult == check )
    assertTrue( "forkJoin works: " + fjResult, fjResult == check )
  } catch basicHandler

}
