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

import org.apache.commons.codec
import com.google.inject
import java.{io => jio}
import org.joda.{time => jtime}
//import scala.collection.JavaConversions._

class FileStrategy ( val path:java.net.URI,
                    objFactory:inject.Provider[model.ObjectSummary.Builder],
                    folderFactory:inject.Provider[model.FolderSummary.Builder]
  ) extends PathStrategy {
    require( path.getScheme == "file", "FileStrategy only support file: URIs")
  val file:jio.File = new jio.File( path.getPath )
  
  /**
   * Internal helper assembles ObjectSummary for the given file
   * 
   * @param file must obey isFile or fails assertion
   */
  def fileSummary( file:java.io.File ):model.ObjectSummary = {
    require( file.isFile, "only accept files" )
    //val md5 = java.security.MessageDigest.getInstance( "MD5" )
    val md5Hex = {
        val in = new jio.FileInputStream( file )
        try {
          codec.digest.DigestUtils.md5Hex( in )
        } finally in.close
    }
    objFactory.get.path( path ).sizeBytes( file.length 
      ).lastModified( new jtime.DateTime( file.lastModified ) 
      ).md5Hex( md5Hex
      ).build    
  }

  
  def ls( file:jio.File ):Option[model.PathSummary] = Some( file ).filter( 
        (file) => file.exists && (file.isFile || file.isDirectory) 
    ).map( _ match {
      case f if f.isFile => fileSummary( f )
      case d if d.isDirectory => {
          val children:Seq[jio.File] = d.listFiles.filterNot( 
            // TODO - make name filter configurable
            (f) => f.getName.endsWith( "~" ) || f.getName.endsWith( ".bak" ) || 
                 f.getName.endsWith( ".old" ) || f.getName.endsWith( "#" )
          )
          folderFactory.get.path( file.toURI ).folders.addAll( children.filter( _.isDirectory ).map( _.toURI )
              ).objects.addAll( children.filter( _.isFile ).map( (f) => fileSummary(f) )
              ).build
      }
  })
  
  
  lazy val ls:Option[model.PathSummary] =  ls(file)
  
  lazy val lsR:Stream[model.PathSummary] = ls.map(
    _ match {
      case folder:model.FolderSummary => folder #:: lsR.map( _.asInstanceOf[model.FolderSummary] 
                                                            ).flatMap( _.folders 
                                                            ).flatMap( (f) => ls( new jio.File( f.getPath )) 
                                                            )
      case obj => Stream(obj)
    }
  ).getOrElse( Stream.empty )

  def copyTo( dest:java.net.URI ):Option[model.ObjectSummary] = ls.map( 
    { throw new UnsupportedOperationException( "not yet implemented" ) } 
  )
  
}

object FileStrategy {

  class Builder @inject.Inject() ( 
    infoFactory:inject.Provider[model.ObjectSummary.Builder],
    summaryFactory:inject.Provider[model.FolderSummary.Builder] 
    ) extends littleware.scala.PropertyBuilder {
      val path = new NotNullProperty[java.net.URI]( null,
           (uri:java.net.URI) => Seq( (uri.getScheme != "file") -> "FileStrategy requires file: URI" ).filter( _._1 ).map( _._2 )
         ).name( "path" )
         
      def build():FileStrategy = {
        this.assertSanity()
        new FileStrategy( path(), infoFactory, summaryFactory )
      }
    }
}
