/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.s3Copy
package controller
package internal
package simplePathTool

import com.google.inject
import java.{io => jio}
import java.net.URI
import java.util.logging.{Level,Logger}

  
class SimpleTool @inject.Inject()( 
  fileStrategyFactory:inject.Provider[FileStrategy.Builder],
  s3StrategyFactory:inject.Provider[S3Strategy.Builder]
) extends PathTool {
  private val log = Logger.getLogger( getClass.getName )
  
  /**
   * Build a strategy to handle the given path,
   * or throw UnsupportedOperationException if path not supported
   */
  def buildStrategy( path:java.net.URI ):PathStrategy = path.getScheme match {
    case "file" => fileStrategyFactory.get.path( path ).build
    case "s3" => s3StrategyFactory.get.path( path ).build
    case scheme => throw new UnsupportedOperationException( "Unsupported path type: " + scheme + " for " + path )
  }
  
  def ls( path:URI ):Option[model.PathSummary] = buildStrategy( path ).ls
  def lsR( path:URI ):Stream[model.PathSummary] = buildStrategy( path ).lsR
  
  //def compare( a:model.FolderSummary, b:model.FolderSummary ):model.FolderDiff 
  def copy( source:URI, dest:URI ):Option[model.ObjectSummary] = {
    val optSrcFile:Option[jio.File] = source.getScheme match {
      case "file" => {
          val f = new jio.File( source.getPath )
          if ( f.exists ) Some(f) else None
      }
      case _ => {
          val temp = jio.File.createTempFile( "s3PathTool", ".copyTemp" )
          buildStrategy( source ).copyTo( temp ).map( _ => temp )
      } 
    }
    optSrcFile.flatMap( (srcFile) => buildStrategy( dest ).copyFrom( srcFile ) )
  }

  def copyUnder( source:URI, destFolder:URI ):Option[model.ObjectSummary] = 
    copy( source, SimpleTool.buildCopyPath( source, destFolder ) )
  
}

object SimpleTool {
  /**
   * Build a dest path that copies the file named by source
   * to destFolder: destFolder / source.getName
   */
  def buildCopyPath( source:java.net.URI, destFolder:java.net.URI ):java.net.URI =
    new java.net.URI(
        destFolder.getScheme,
        destFolder.getHost,
        (destFolder.getPath + "/" + PathTool.baseName( source )).replaceAll( "//+", "/" ), 
        null
      )

}