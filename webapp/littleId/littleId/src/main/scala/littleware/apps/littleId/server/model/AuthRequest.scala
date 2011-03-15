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

import littleware.apps.littleId.OIdProvider
import littleware.base.validate.ValidatorUtil
import littleware.scala.LittleHelper


/**
 * User-supplied data for open-id authentication
 */
trait AuthRequest {
  val openIdProvider:OIdProvider.Value
  val clientSecret:String
}

object AuthRequest {
  private case class SimpleRequest(
    @scala.reflect.BeanProperty
    openIdProvider:OIdProvider.Value,
    @scala.reflect.BeanProperty
    clientSecret:String
  ) extends AuthRequest with java.io.Serializable {
    ValidatorUtil.check(
      null != openIdProvider, "openIdProvider set"
    )
    ValidatorUtil.check(
      LittleHelper.emptyCheck( clientSecret ).isDefined,
      "client secret set"
    )

  }

  def apply(   openIdProvider:OIdProvider.Value,
            clientSecret:String
  ) {
    SimpleRequest( openIdProvider, clientSecret )
  }
}
