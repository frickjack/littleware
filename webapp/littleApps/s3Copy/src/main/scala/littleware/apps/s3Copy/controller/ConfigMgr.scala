/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.s3Copy
package controller

import com.amazonaws.services.s3
import com.google.inject

/**
 * Tracks configuratoin that may be modified by the user at runtime.
 * May attempt to persist configuration between runs depending on
 * the implementation.
 */
trait ConfigMgr {
  def   s3Config:Option[model.config.S3Config]
  def   s3Config( value:model.config.S3Config ):this.type
  
  /**
   * Factory supplies S3 clients via config
   */
  val   s3Factory:ConfigMgr.S3Factory
}

object ConfigMgr {
  trait S3Factory extends inject.Provider[s3.AmazonS3] {}
}
