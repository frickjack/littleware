/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available for use
 * subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.model.internal

import java.net.URL
import littleware.apps.littleId
import littleId.server.model

object AuthRequestBuilder {
  case class SimpleRequest(
    @scala.reflect.BeanProperty
    openIdProvider:littleId.common.model.OIdProvider.Value,
    @scala.reflect.BeanProperty
    replyTo:URL,
    @scala.reflect.BeanProperty
    replyMethod:model.AuthRequest.ReplyMethod.Value
  ) extends model.AuthRequest with java.io.Serializable {
    require(
      null != openIdProvider, "openIdProvider may not be null"
    )
    require(
      null != replyTo, "replyToURL may not be null"
    )

  }
  
}

class AuthRequestBuilder extends model.AuthRequest.Builder {

  def build():model.AuthRequest = AuthRequestBuilder.SimpleRequest( openIdProvider, replyTo, replyMethod )
}
