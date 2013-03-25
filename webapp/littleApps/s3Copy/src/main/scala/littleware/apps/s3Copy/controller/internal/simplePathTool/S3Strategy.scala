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


import com.amazonaws.services.s3
import com.google.common.{io => gio}
import com.google.inject
import java.{io => jio}
import java.net.URI
import java.util.logging.{Level,Logger}
import java.util.zip
import org.joda.{time => jtime}
import scala.collection.JavaConversions._
import scala.util.{Try,Success,Failure}

/**
 * Strategy for s3Copy dealing with S3 URI's.
 * Note - uses s3Factory to retrieve fresh
 * s3 client on each use, since S3 configuration may
 * change over lifetime of application
 */
class S3Strategy ( val path:java.net.URI,
                    objFactory:inject.Provider[model.ObjectSummary.Builder],
                    folderFactory:inject.Provider[model.FolderSummary.Builder],
                    s3Factory:inject.Provider[s3.AmazonS3]
) extends PathStrategy {
  require( path.getScheme == "s3", "S3Strategy only support s3: URIs")  
  private val log = Logger.getLogger( getClass.getName )
  val s3Bucket:String = path.getHost
  val s3Path:String = ("/" + path.getPath).replaceAll( "//+", "/" 
    ).replaceAll( "/+$", "" )

  
  /**
   * Factory for AWS exception handler that tests for HTTP 404 (does not exist)
   */
  private def ex404Handler[T]:PartialFunction[Throwable,Option[T]] = {
    case ex:com.amazonaws.AmazonServiceException if ex.getStatusCode == 404 => None
    case ex => {
      log.log( Level.WARNING, "Failed S3 call", ex )
      throw ex
    }
  }
  
  /**
   * Internal helper to list contents of a path that
   * we know is a folder - non recursive
   */
  def lsFolder( s3Client:s3.AmazonS3, folder:java.net.URI ):model.FolderSummary = {
    if( log.isLoggable(Level.FINE) ) log.log( Level.FINE, "lsFolder(" + folder + ")" )
      // no object at the path, so check if it's a folder (s3 prefix)
      lazy val s3Listing:Stream[s3.model.ObjectListing] = 
          s3Client.listObjects( 
            new s3.model.ListObjectsRequest( folder.getHost, folder.getPath.replaceAll( "^/+", "" ), null, "/", null ) 
          ) #:: s3Listing.takeWhile( _.isTruncated ).map( (part) => s3Client.listNextBatchOfObjects( part ) )

      val folderBuilder = folderFactory.get.path( folder )
      s3Listing.flatMap( _.getObjectSummaries ).filter( _.getETag != null ).foreach( (obj) => {
          folderBuilder.objects.add(
            objFactory.get.path( 
              PathTool.s3Path( s3Bucket, obj.getKey )
                            ).md5Hex( obj.getETag 
                            ).lastModified( new jtime.DateTime( obj.getLastModified )
                            ).sizeBytes( obj.getSize 
                            ).build
            )
        })
      s3Listing.flatMap( _.getCommonPrefixes ).foreach( (pre) => {
          folderBuilder.folders.add( PathTool.s3Path( s3Bucket, pre ))
        })
      val result = folderBuilder.build
      //log.info( "lsFolder Result: " + result )
      result
  }
  
  
  lazy val ls:Option[model.PathSummary] = {
    val s3Client = s3Factory.get
    
    // first check if there is an object at the path
    val optObject:Option[model.PathSummary] = Try( s3Client.getObjectMetadata( s3Bucket, s3Path )) match {
      case Success(meta) if ( meta.getETag != null ) => {
          Some( objFactory.get.path( path
                  ).md5Hex( meta.getETag
                  ).sizeBytes( meta.getContentLength 
                  ).lastModified( new jtime.DateTime( meta.getLastModified )
                  ).build 
             )
      }
      case Failure(ex) => ex404Handler[model.PathSummary](ex)
      case Success(meta) => {
          //if( log.isLoggable( Level.FINE ) ) log.log( Level.FINE, "S3 metadata: " + meta.getContentType )
          None
      }
    }

    if ( optObject.isDefined ) optObject else Some( lsFolder( s3Client, path ) )
  }
  
  val lsR:Stream[model.FolderSummary] = {
    val s3Client = s3Factory.get
    def doStream( uriStream:Stream[java.net.URI] ):Stream[model.FolderSummary] = {
      val summaryStream = uriStream.map( (uri) => lsFolder(s3Client,uri) )
      summaryStream ++ summaryStream.flatMap( (summary) => doStream( summary.folders.toStream ) )
    }
    
    /*
    lazy val stream:Stream[model.FolderSummary] = lsFolder( s3Client, path ) #:: 
      stream.flatMap( _.folders ).map( lsFolder( s3Client, _ ) ) 
    stream
    */
   doStream( Stream( path ) )
  }
  
  //def compare( a:model.FolderSummary, b:model.FolderSummary ):model.FolderDiff 
  def copyTo( destFile:java.io.File ):Option[model.ObjectSummary] = {
    val s3Client = s3Factory.get
    try {
      val obj = s3Client.getObject( s3Bucket, s3Path )
      val istream = if ( "gzip".equalsIgnoreCase( obj.getObjectMetadata.getContentEncoding ) ) {
        new zip.GZIPInputStream( obj.getObjectContent )
      } else {
        obj.getObjectContent
      }
      try {
        val ostream = new jio.FileOutputStream( destFile )
        try {
          gio.ByteStreams.copy( istream, ostream )
        } finally ostream.close
      } finally istream.close
      Some( FileStrategy.fileSummary( destFile, objFactory.get ).build )
    } catch ex404Handler
  }

  def copyFrom( source:java.io.File ):Option[model.ObjectSummary] = {
    throw new UnsupportedOperationException( "not yet implemented" )
  }
}

object S3Strategy {
  class Builder @inject.Inject() ( 
    infoFactory:inject.Provider[model.ObjectSummary.Builder],
    summaryFactory:inject.Provider[model.FolderSummary.Builder],
    s3Factory:inject.Provider[s3.AmazonS3]
    ) extends littleware.scala.PropertyBuilder {
      val path = new NotNullProperty[java.net.URI]( null,
           (uri:java.net.URI) => 
           Seq( ((uri.getScheme != "file") && (uri.getScheme != "s3")) -> 
               "Strategy requires 'file:' or 's3:' scheme URI" 
              ).filter( _._1 ).map( _._2 )
         ).name( "path" )
         
      def build():S3Strategy = {
        this.assertSanity()
        new S3Strategy( path(), infoFactory, summaryFactory, s3Factory )
      }
    }
  
}
