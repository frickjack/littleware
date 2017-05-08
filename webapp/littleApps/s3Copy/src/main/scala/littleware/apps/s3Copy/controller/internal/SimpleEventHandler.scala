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
   * uri below a given root uri removing leading "/" from result unless
   * path==root, then just return path.basename
   */
  def basepath( path:java.net.URI, root:java.net.URI ):String = {
      val pathPath = path.getPath
      val rootPath = root.getPath
      require( pathPath.length >= rootPath.length, "valid looking path and root: " + path + " & " + root )
      if ( pathPath.length > rootPath.length ) {
        pathPath.substring( rootPath.length ).replaceAll( "^/+", "" )
      } else {
        val lastSlash = pathPath.lastIndexOf( '/' )
        if ( lastSlash > -1 ) {
          pathPath.substring( lastSlash+1 )
        } else pathPath
      }
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
      
      val destRootStr = destRoot.toString.replaceAll( "/+$", "" ) + "/"
      
      val destObjects:Seq[model.ObjectSummary] = if( srcObjects.size == 1 ) {
        // single object copy - just check if dest already exists
        srcObjects.flatMap( 
          (o) => {
            val destUri = new java.net.URI( destRootStr + basepath( o.path, srcRoot ) )
            tool.ls( destUri )
          } ).flatMap( pathToObjs(_) )
      } else tool.lsR( destRoot ).flatMap( pathToObjs(_)  )
      
      // sub-destRoot path to object map
      val destIndex:Map[java.net.URI,model.ObjectSummary] = destObjects.map( 
        o => o.path -> o
      ).toMap
      fb.setProgress( 66 )
      fb.info( "Merging source and destination index ...")
      
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
