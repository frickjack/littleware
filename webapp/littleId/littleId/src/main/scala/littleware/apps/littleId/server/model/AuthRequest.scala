/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.model

import java.net.URL
import littleware.apps.littleId.OIdProvider


/**
 * User-supplied data for open-id authentication
 */
trait AuthRequest {
  val openIdProvider:OIdProvider.Value
  val replyTo:URL
  val replyMethod:AuthRequest.ReplyMethod.Value
}

object AuthRequest {
  object ReplyMethod extends Enumeration {
    val GET, POST = Value
  }

  trait Builder {
    var openIdProvider:OIdProvider.Value = null
    def openIdProvider( value:OIdProvider.Value ):this.type = {
      openIdProvider = value
      this
    }

    var replyTo:URL = null
    def replyTo( value:URL ):this.type = {
      replyTo = value
      this
    }

    var replyMethod:AuthRequest.ReplyMethod.Value = ReplyMethod.POST
    def replyMethod( value:ReplyMethod.Value ):this.type = {
      replyMethod = value
      this
    }

    def build():AuthRequest
  }

  
  
}
