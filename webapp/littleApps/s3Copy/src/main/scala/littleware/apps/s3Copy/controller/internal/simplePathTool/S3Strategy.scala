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
import org.apache.commons.codec
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
                    s3Factory:inject.Provider[s3.AmazonS3],
                    mimeMap:javax.activation.MimetypesFileTypeMap
) extends PathStrategy {
  import S3Strategy._
  
  require( path.getScheme == "s3", "S3Strategy only support s3: URIs")  
  private val log = Logger.getLogger( getClass.getName )
  val s3Bucket:String = path.getHost
  val s3Path:String = cleanPath( path.getPath )

  
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
      val cleanFolderPath:String = cleanPath( folder.getPath ).trim
      val queryFolderPath:String = if ( cleanFolderPath.nonEmpty ) cleanFolderPath + "/" 
          else cleanFolderPath
      lazy val s3Listing:Stream[s3.model.ObjectListing] = 
          s3Client.listObjects( 
            new s3.model.ListObjectsRequest( folder.getHost, queryFolderPath, null, "/", null ) 
          ) #:: s3Listing.takeWhile( _.isTruncated ).map( (part) => s3Client.listNextBatchOfObjects( part ) )

      val folderBuilder = folderFactory.get.path( folder )
      s3Listing.flatMap( _.getObjectSummaries ).filter( _.getETag != null ).foreach( 
        (obj) => {
          folderBuilder.objects.add(
            decorate( 
              objFactory.get.path( PathTool.s3Path( s3Bucket, obj.getKey ) ), 
              obj ).build
            )
        })
      s3Listing.flatMap( _.getCommonPrefixes 
        ).filter( cleanPath(_) != cleanFolderPath ).foreach( (pre) => {
          folderBuilder.folders.add( PathTool.s3Path( s3Bucket, pre ))
        })
      val result = folderBuilder.build
      //log.info( "lsFolder Result: " + result )
      result
  }
  
  
  def s3ObjectMetadata(  path:java.net.URI ):Option[s3.model.ObjectMetadata] = {
    val s3Client = s3Factory.get
    val s3Bucket:String = path.getHost
    val s3Path:String = cleanPath( path.getPath )
    // first check if there is an object at the path
    Try( s3Client.getObjectMetadata( s3Bucket, s3Path )) match {
      case Success(meta) if ( meta.getETag != null ) => {
          Some( meta )
      }
      case Failure(ex) => ex404Handler[s3.model.ObjectMetadata](ex)
      case Success(meta) => {
          //if( log.isLoggable( Level.FINE ) ) log.log( Level.FINE, "S3 metadata: " + meta.getContentType )
          None
      }
    }
  }
  
  def ls( path:java.net.URI ):Option[model.PathSummary] = {
    val s3Client = s3Factory.get
    val optObject = s3ObjectMetadata( path 
       ).map( (meta) => decorate( objFactory.get.path( path ), meta ).build )

    if ( optObject.isDefined ) optObject else 
      Some( lsFolder( s3Client, path ) ).filter( (s) => s.folders.nonEmpty || s.objects.nonEmpty )
  }
  
  lazy val ls:Option[model.PathSummary] = ls(path)
  
  val lsR:Stream[model.FolderSummary] = {
    val s3Client = s3Factory.get
    
    def folderStream( folder:java.net.URI ):Stream[model.FolderSummary] = {
      val f = lsFolder( s3Client, folder )
      f #:: f.folders.toStream.flatMap( 
        (furi) => folderStream(furi) 
      )
    }
    
     folderStream( path )                                                                      
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
    val gzipTemp = {
        val f = jio.File.createTempFile( "gzipTemp", "." + new jio.File( path.getPath ).getName )
        val in = new jio.FileInputStream( source )
        try {
          val zipOut = new zip.GZIPOutputStream( new jio.FileOutputStream( f ))
          try {
            gio.ByteStreams.copy( in, zipOut )
          } finally zipOut.close
        } finally in.close
        f
      }
    
    val gzipMd5Hex = {
        val in = new jio.FileInputStream( gzipTemp )
        try {
          codec.digest.DigestUtils.md5Hex( in )
        } finally in.close
    }
    
    // check if copy in S3 is already in sync with source
    val md5Check = ls.map( _.asInstanceOf[model.ObjectSummary] 
       ).filter( _.md5Hex == gzipMd5Hex 
       )
    
    if ( md5Check.isDefined ) {
      log.log( Level.FINE, "Skipping copy - md5 checks in sync: {0}", path )
      md5Check 
    } else {
      try {
        val s3Client = s3Factory.get
        val request = new s3.model.PutObjectRequest( s3Bucket, s3Path, gzipTemp )
        val meta = new s3.model.ObjectMetadata()
        meta.setContentEncoding( "gzip" )
        request.setMetadata( meta )
        s3Client.putObject(request)
      } finally gzipTemp.delete
      ls( path ).map( _.asInstanceOf[model.ObjectSummary])
    }
  }
}

object S3Strategy {

  /**
   * Clean up leading / and trailing / and duplicate /
   */
  def cleanPath( path:String ):String = path.replaceAll( "//+", "/" 
    ).replaceAll( "/+$", "" ).replaceAll( "^/+", "" )
    
  /**
   * Decorate the object builder with md5, modify date, and size meta data
   * from S3
   */
  def decorate( builder:model.ObjectSummary.Builder, meta:s3.model.ObjectMetadata ):model.ObjectSummary.Builder =
    builder.md5Hex( meta.getETag 
            ).sizeBytes( meta.getContentLength 
            ).encoding( Option( meta.getContentEncoding ).filter( _.nonEmpty )
            ).lastModified( new jtime.DateTime( meta.getLastModified ) )

  def decorate( builder:model.ObjectSummary.Builder, meta:s3.model.S3ObjectSummary 
    ):model.ObjectSummary.Builder = 
    builder.md5Hex( meta.getETag 
            ).sizeBytes( meta.getSize
            ).encoding.set( "gzip" // assume everything has gzip encoding in S3
            ).lastModified( new jtime.DateTime( meta.getLastModified ) )

    
  class Builder @inject.Inject() ( 
    infoFactory:inject.Provider[model.ObjectSummary.Builder],
    summaryFactory:inject.Provider[model.FolderSummary.Builder],
    s3Factory:inject.Provider[s3.AmazonS3],
    mimeMap:javax.activation.MimetypesFileTypeMap
    ) extends littleware.scala.PropertyBuilder {
      val path = new NotNullProperty[java.net.URI]( null,
           (uri:java.net.URI) => 
           Seq( ((uri.getScheme != "file") && (uri.getScheme != "s3")) -> 
               "Strategy requires 'file:' or 's3:' scheme URI" 
              ).filter( _._1 ).map( _._2 )
         ).name( "path" )
         
      def build():S3Strategy = {
        this.assertSanity()
        new S3Strategy( path(), infoFactory, summaryFactory, s3Factory, mimeMap )
      }
    }
  
}
