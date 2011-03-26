/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.common.model

trait VerifyResponse {
  val isDataValid:Boolean
}

object VerifyResponse {
  private case class SimpleResponse(
    @scala.reflect.BeanProperty
    isDataValid:Boolean
  ) extends VerifyResponse {}

  def apply( isDataValid:Boolean ):VerifyResponse = SimpleResponse( isDataValid )
}
