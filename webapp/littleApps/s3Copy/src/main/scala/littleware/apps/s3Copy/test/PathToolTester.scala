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
import com.google.common.{io => gio}
import java.{io => jio}
import junit.framework.Assert._
import littleware.scala.test.LittleTest

class PathToolTester @inject.Inject()(
  tool:controller.PathTool
) extends LittleTest {
  setName( "testPathLs" )
  
  var s3TestFolder:java.net.URI = new java.net.URI( "s3://apps.frickjack.com/" )
  def withS3TestFolder( value:java.net.URI ):this.type = {
    s3TestFolder = value
    this
  }
  
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
  
  def testPathLs():Unit = try {
    Seq( new jio.File( "." ).toURI,
         s3TestFolder
     ).foreach(
        (testPath) => {
          tool.ls( testPath ).foreach( 
            (summary) => { 
              summaryTest( summary )
              summary match {
                case folder:model.FolderSummary => {
                    assertTrue( "Folder is not empty: " + folder.path,
                               folder.objects.nonEmpty && folder.folders.nonEmpty
                               )
                }
              }
            }
          )

          tool.lsR( testPath ).filter( 
            (summary) => Seq( ".*ivy/?$", ".*cli/?$", ".*PathToolTester/?$" ).find( (rx) => summary.path.getPath.matches( rx ) ).isEmpty 
          ).take(3).foreach( (summary) => summaryTest( summary ))
        }
      )
  } catch basicHandler
  
  
  def testPathCopy():Unit = try {
    val testPath = new jio.File( "." ).toURI
        
    tool.ls( testPath ).foreach( 
      (summary) => { 
        summaryTest( summary )
        summary match {
          case folder:model.FolderSummary => {
              assertTrue( "Folder is not empty: " + folder.path,
                         folder.objects.nonEmpty && folder.folders.nonEmpty
                         )
              val tempFolder = gio.Files.createTempDir.toURI
              folder.objects.foreach( (f) => {
                  val optOut = tool.copyUnder( f.path, tempFolder )
                  log.info( "Copied " + f + " to " + tempFolder )
                  assertTrue( 
                    "Copy worked as expected: " + f + " -> " + optOut.getOrElse( "?" ),
                    optOut.map( 
                      (summary:model.PathSummary) => new jio.File( summary.path ).exists  
                    ).getOrElse( false )
                  )
                })
          }
        }
      }
    )    
  } catch basicHandler
  
  def testPathParts():Unit = try {
    Map( new java.net.URI( "s3://server/a/b/c/" ) -> "c"
      ).foreach( _ match {
          case (path,expected) => {
              val baseName = controller.PathTool.baseName( path )
              assertTrue( path.toString + " got expected baseName: " + baseName,
                         baseName == expected
                )
          }
        }
      )
    Map( (new jio.File( "./bla.txt" ).toURI, new java.net.URI( "s3://a/b/c" ) ) -> 
        new java.net.URI( "s3://a/b/c/bla.txt")
    ).foreach( _ match {
        case ((source,folder),expected) => {
            val dest = controller.internal.simplePathTool.SimpleTool.buildCopyPath(source, folder)
            assertTrue( "Got expected copy destination: " + dest, dest == expected )
        }
      })
  } catch basicHandler
  
}
