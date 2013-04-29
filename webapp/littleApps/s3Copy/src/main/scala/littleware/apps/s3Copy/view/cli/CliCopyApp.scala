/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.s3Copy
package view.cli

import com.google.inject
import java.util.logging.{Level,Logger}
import littleware.scala.GetoptHelper

class CliCopyApp @inject.Inject() ( 
  val boot:littleware.bootstrap.LittleBootstrap,
  configBuilder:model.config.S3Config.Builder,
  handler:controller.UIEventHandler,
  fb:littleware.base.feedback.Feedback
) {
  val log = Logger.getLogger( getClass.getName )
  import CliCopyApp.useHelp
  
  def doConfig( params:Seq[String] ):Unit = {
    require( params.size == 2, useHelp )
    val config = configBuilder.creds(
      new com.amazonaws.auth.BasicAWSCredentials( params(0), params(1) )
    ).build
    handler.updateConfig(config)
    fb.info( "AWS configuration saved" )
  }
  
  def doCopy( params:Seq[String] ):Unit = {
    require( params.size == 2, useHelp )
    val uris = params.map( 
      (p) => if( p.startsWith( "file:" ) || p.startsWith( "s3:" ) ) new java.net.URI( p )
      else new java.io.File( p ).getCanonicalFile.toURI
             )
             
    val infoSeq = handler.listCommands( uris(0), uris(1), fb.nested(30,100) )
    infoSeq.foreach( summary => fb.info( summary.toString ))
    System.out.println( "Hit any key to run copy:" )
    new java.io.BufferedReader(
      new java.io.InputStreamReader( System.in, littleware.base.Whatever.UTF8 )
      ).readLine
    handler.copy( infoSeq, fb.nested(70,100) )
  }
  
  def go( args:Array[String] ):Unit = {
    val opts = GetoptHelper.extract( args )
    require( opts.size == 1, useHelp )
    opts.head match {
      case ("config", params) => doConfig( params )
      case ("copy", params) => doCopy( params )
      case _ => System.out.println( useHelp )
    }
  }
}

object CliCopyApp {
  val log = Logger.getLogger( getClass.getName )
  
  val useHelp:String = """
  s3cp -config <awsAccessKey> <awsSecretKey>
  s3cp -copy <source> <destination>
  """
  
  def main( args:Array[String] ):Unit = {
    val app = littleware.bootstrap.AppBootstrap.appProvider.get.build.bootstrap( classOf[CliCopyApp] )
    val exitCode = try {
      app.go( args )
      0
    } catch {
      case ex:Throwable => {
          log.log( Level.WARNING, "Runtime error", ex )
          1
      }
    } finally app.boot.shutdown
    System.exit( exitCode )
  }
}