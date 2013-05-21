/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId
package client
package controller

import collection.JavaConversions._

trait VerifyTool {
  /**
   * Verify the given credentials with the given secret
   */
  def verify( secret:String ):Option[common.model.OIdUserCreds]
}
