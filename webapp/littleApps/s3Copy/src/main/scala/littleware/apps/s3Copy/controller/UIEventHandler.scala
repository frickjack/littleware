package littleware.apps.s3Copy
package controller

import littleware.base.feedback.Feedback


/**
 * Generic handlers for events common to most UI
 */
trait UIEventHandler {
  import UIEventHandler._
  
  /**
   * Apply the given configuration to the ConfigMgr, and save
   * the new config to LittleModuleFactory.awsKeyResource
   */
  def updateConfig( s3Config:model.config.S3Config ):Unit
  
  /**
   * List copy source-destination
   */
  def listCommands( srcRoot:java.net.URI, 
                   destRoot:java.net.URI,
                   fb:Feedback
    ):Seq[SrcDestSummary]
    
  def copy( srcDestSeq:Seq[SrcDestSummary], fb:Feedback ):Unit
}

object UIEventHandler {
  
  case class SrcDestSummary(
    src:model.ObjectSummary,
    destPath:java.net.URI,
    destSummary:Option[model.ObjectSummary]
  );
  
}