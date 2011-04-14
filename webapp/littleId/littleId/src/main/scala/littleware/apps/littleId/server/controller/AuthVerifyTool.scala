/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.controller

import littleware.apps.littleId
import littleId.server.model

/**
 * Little tools for clients to verify authentication with
 */
trait AuthVerifyTool {
  /**
   * Add the authentication data to the verification database.
   * A new authentication is cached for at most 5 minutes, and only
   * supports one verification.
   */
  def cacheCreds( secret:String, creds:littleId.common.model.OIdUserCreds ):Unit

  /**
   * Verify the given credentials against the verification data cache
   */
  def verifyCreds( secret:String, creds:Map[String,String] ):Boolean
}
