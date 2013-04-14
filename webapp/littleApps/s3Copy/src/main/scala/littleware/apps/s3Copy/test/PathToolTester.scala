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
  tool:controller.PathTool,
  mimeMap:javax.activation.MimetypesFileTypeMap
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
              assertTrue( "ls " + summary.path + " has subfolders or objects", 
                         folderSummary.folders.nonEmpty || folderSummary.objects.nonEmpty 
              )
              val cleanPath = folderSummary.path.getPath.replaceAll( "/+", "" )
              assertTrue( "Folder does not include itself as a subfolder", 
                         folderSummary.folders.map( _.getPath.replaceAll( "/+", "" ) ).find( _ == cleanPath ).isEmpty 
              )
          }
          case err => fail( "Unexpected ls result: " + err )
      }
  }
  
  def testMimeTypes():Unit = try {
    Seq( "json" -> "application/json", "html" -> "text/html", 
        "js" -> "application/javascript", "png" -> "image/png"
    ).foreach( _ match {
        case (suffix,mtype) => {
            val lookup = mimeMap.getContentType( "bla." + suffix )
            assertTrue( "Got expected mime type: " + lookup + " ?= " + mtype, lookup == mtype )
        }
      })
  } catch basicHandler
  
  
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
    val destPaths = Seq(
      gio.Files.createTempDir.toURI,
      new java.net.URI( "s3://apps.frickjack.com/testSuite/PathToolTester/testPathCopy" )
    )    
    destPaths.foreach( (tempFolder) => {
      tool.ls( testPath ).foreach( 
        (summary) => { 
          summaryTest( summary )
          summary match {
            case folder:model.FolderSummary => {
                assertTrue( "Folder is not empty: " + folder.path,
                           folder.objects.nonEmpty && folder.folders.nonEmpty
                           )
                folder.objects.foreach( (f) => {
                    val optOut = tool.copyUnder( f.path, tempFolder )
                    log.info( "Copied " + f + " to " + tempFolder )
                    assertTrue( 
                      "Copy worked as expected: " + f + " -> " + optOut.getOrElse( "?" ),
                      optOut.isDefined
                    )
                  })
            }
          }
        }
      )    
    })
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
