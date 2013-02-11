/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.s3Copy
package test


import com.google.inject
import java.{io => jio}
import junit.framework.Assert._
import littleware.scala.test.LittleTest

class PathToolTester @inject.Inject()(
  tool:controller.PathTool
) extends LittleTest {
  setName( "testPathTool" )
  
  def testPathTool():Unit = try {
    val testPath = new jio.File( "." ).toURI
    def summaryTest( summary:model.PathSummary ):Unit = {
        log.info( summary.path.toString + " summary: " + summary )
        summary match {
          case folderSummary:model.FolderSummary => {
              assertTrue( "ls " + summary.path + " has subfolders", folderSummary.folders.nonEmpty )
              assertTrue( "ls " + summary.path + " has child objects", folderSummary.objects.nonEmpty )
          }
          case err => fail( "Unexpected ls result: " + err )
        }
      }
    
    tool.ls( testPath ).foreach( (summary) => summaryTest( summary ))
    tool.lsR( testPath ).filter( ! _.path.getPath.matches( ".+ivy/?$" ) ).take(3).foreach( (summary) => summaryTest( summary ))
  } catch basicHandler
  
}
