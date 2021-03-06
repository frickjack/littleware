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
import com.google.common.{io => gio}
import java.{io => jio}
import java.util.logging.{Level,Logger}
import org.apache.commons.codec
import org.joda.{time => jtime}
//import scala.collection.JavaConversions._

class FileStrategy ( val path:java.net.URI,
                    objFactory:inject.Provider[model.ObjectSummary.Builder],
                    folderFactory:inject.Provider[model.FolderSummary.Builder]
  ) extends PathStrategy {
    require( path.getScheme == "file", "FileStrategy only support file: URIs")
  val file:jio.File = new jio.File( path.getPath ).getCanonicalFile
  val log = Logger.getLogger( getClass.getName )
  
  /**
   * Internal helper assembles ObjectSummary for the given file
   * 
   * @param file must obey isFile or fails assertion
   */
  def fileSummary( file:java.io.File ):model.ObjectSummary = FileStrategy.fileSummary( file, objFactory.get ).build
  
  def ls( file:jio.File ):Option[model.PathSummary] = Some( file.getCanonicalFile ).filter( 
        (file) => file.exists && (file.isFile || file.isDirectory) 
    ).map( _ match {
      case f if f.isFile => fileSummary( f )
      case d if d.isDirectory => {
          val children:Seq[jio.File] = d.listFiles.filterNot( 
            // TODO - make name filter configurable
            (f) => f.getName.endsWith( "~" ) || f.getName.endsWith( ".bak" ) || 
                 f.getName.endsWith( ".old" ) || f.getName.endsWith( "#" )
          )
          folderFactory.get.path( d.toURI ).folders.addAll( children.filter( _.isDirectory ).map( _.toURI )
              ).objects.addAll( children.filter( _.isFile ).map( (f) => fileSummary(f) )
              ).build
      }
  })
  
  
  lazy val ls:Option[model.PathSummary] =  ls(file)
  
  lazy val lsR:Stream[model.PathSummary] = ls.map(
    _ match {
      case folder:model.FolderSummary => {
          
          val root = new java.io.File( folder.path.getPath ).getCanonicalFile
          //case class State( accum:List[jio.File], stack:List[jio.File] )
          
          def depthFirst( stack:List[jio.File] ):Stream[jio.File] = {
            stack match {
              case head :: tail => {
                  head #:: depthFirst( tail ++ head.listFiles.filter( _.isDirectory ) )
              }
              case Nil => Stream.empty
            }
          }
          
            /*
          lazy val temp:Stream[jio.File] = new java.io.File( folder.path.getPath ).getCanonicalFile #:: 
            temp.flatMap( (d) => { log.fine( "Scanning: " + d ); d.listFiles().filter( (sd) => sd.isDirectory && sd != d ) } )
            */
          depthFirst( List( root ) ).map( ls(_).get )
      }
      case obj => Stream(obj)
    }
  ).getOrElse( Stream.empty )

  /** Internal helper */
  private def copy( source:jio.File, dest:jio.File ):Option[model.ObjectSummary] = 
    Option(source).filter( (source) => source.exists ).flatMap(
      (source) => {
          require( source.isFile, "copy only operates on files: " + source )
          require( source != dest, "copy source and destination must differ: " + source )
          gio.Files.copy( source, dest )
          ls( dest ).map( _.asInstanceOf[model.ObjectSummary] )
        }
      )
  
  
  def copyTo( dest:java.io.File ):Option[model.ObjectSummary] = 
    copy( new jio.File( path.getPath ), dest )
  
  def copyFrom( source:java.io.File ):Option[model.ObjectSummary] =
    copy( source, new jio.File( path.getPath ) )
  
}

object FileStrategy {
  /**
   * Internal (to module) helper assembles ObjectSummary for the given file
   * 
   * @param file must obey isFile or fails assertion
   * @param builder to set properties on
   * @return builder
   */
  private[s3Copy] def fileSummary( file:java.io.File, builder:model.ObjectSummary.Builder ):model.ObjectSummary.Builder = {
    require( file.isFile, "only accept files" )
    //val md5 = java.security.MessageDigest.getInstance( "MD5" )
    val md5Hex = {
        val in = new jio.FileInputStream( file )
        try {
          codec.digest.DigestUtils.md5Hex( in )
        } finally in.close
    }
    builder.path( file.getCanonicalFile.toURI ).sizeBytes( file.length 
      ).lastModified( new jtime.DateTime( file.lastModified ) 
      ).md5Hex( md5Hex
      )
  }

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
