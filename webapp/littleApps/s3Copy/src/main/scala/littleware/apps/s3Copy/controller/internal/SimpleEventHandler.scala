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

import com.google.inject
import java.util.logging.{Level,Logger}
import littleware.base.feedback.Feedback


class SimpleEventHandler @inject.Inject() (
  tool:PathTool,
  configMgr:ConfigMgr,
  propLoader:littleware.base.PropertiesLoader
  ) extends UIEventHandler {
  import UIEventHandler._
  private val log = Logger.getLogger( getClass.getName )
  
  
  def updateConfig( s3Config:model.config.S3Config ):Unit = {
    configMgr.s3Config( s3Config )
    propLoader.safelySave( model.config.S3Config.credsToProps(s3Config.creds), 
                          new java.io.File( propLoader.getLittleHome.toString + "/" + littleModule.Config.awsKeysResource )
    )
  }
  

  /** 
   * Internal utility - extract the path portion of a given  
   * uri below a given root uri removing leading "/" from result
   */
  def basepath( path:java.net.URI, root:java.net.URI ):String = {
      val pathPath = path.getPath
      val rootPath = path.getPath
      require( pathPath.length >= rootPath.length, "valid looking path and root: " + path + " & " + root )
      pathPath.substring( rootPath.length ).replaceAll( "^/+", "" )
  }
  
  def listCommands( srcRoot:java.net.URI, 
                   destRoot:java.net.URI,
                   fb:Feedback
    ):Seq[SrcDestSummary] = try {
      fb.info( "Scanning " + srcRoot )
      val pathToObjs:PartialFunction[model.PathSummary,Seq[model.ObjectSummary]] = {
          case o:model.ObjectSummary => Seq(o)
          case f:model.FolderSummary => f.objects
      }
      
      val srcObjects:Seq[model.ObjectSummary] = tool.lsR( srcRoot ).flatMap( pathToObjs(_) )
      fb.setProgress( 33 )
      fb.info( "Scanning " + destRoot )
      val destObjects = tool.lsR( destRoot ).flatMap( pathToObjs(_)  )
      // sub-destRoot path to object map
      val destIndex:Map[java.net.URI,model.ObjectSummary] = destObjects.map( 
        o => o.path -> o
      ).toMap
      fb.setProgress( 66 )
      fb.info( "Merging source and destination index ...")
      val destRootStr = destRoot.toString.replaceAll( "/+$", "" ) + "/"
      srcObjects.map( o => {
          val destUri = new java.net.URI( destRootStr + basepath( o.path, srcRoot ) )
          SrcDestSummary( o, destUri, destIndex.get( destUri ))
        }
      )
    } finally fb.setProgress(100)
    


  def copy( srcDestSeq:Seq[SrcDestSummary], fb:Feedback ):Unit = 
    srcDestSeq.iterator.zipWithIndex.foreach( _ match {
        case (info,index) => {
            fb.setProgress( index, srcDestSeq.size )
            fb.info( "Copying " + info.src.path + " to " + info.destPath )
            tool.copy( info.src.path, info.destPath )
        }
      })
}
