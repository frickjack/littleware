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
import java.net.URI


/**
 * Strategy for PathTool to deal with a particular path.
 * Different strategy implementations for different URI types.
 */
trait PathStrategy {
  val path:java.net.URI
  val ls:Option[model.PathSummary]
  val lsR:Stream[model.PathSummary]
  
  //def compare( a:model.FolderSummary, b:model.FolderSummary ):model.FolderDiff 
  def copyTo( dest:URI ):Option[model.ObjectSummary]
}

object PathStrategy {
  class Builder @inject.Inject()( 
        s3Factory:ConfigMgr.S3Factory,
        fsProvider:inject.Provider[FileStrategy.Builder]
    ) extends littleware.scala.PropertyBuilder {
    val path = new NotNullProperty[java.net.URI]()
    
    def build():PathStrategy = {
      assert( path.checkSanity.isEmpty, 
             "Valid path for PathStrategy: " + path.checkSanity.mkString(", " ) 
      )
      path().getScheme match {
        case "file" => fsProvider.get.path( path() ).build
        //case "s3"   => new S3Strategy( path() )
        case sc => throw new UnsupportedOperationException( "Unsupported URI scheme: " + sc + ": " + path() )
      }
    }
  }
  
}