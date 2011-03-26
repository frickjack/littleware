/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.common.model.internal

import java.net.URL
import littleware.apps.littleId.common.model.OIdUserCreds

object OIdUserBuilder {
  case class SimpleOIdUser(
    @scala.reflect.BeanProperty
    email:String,
    @scala.reflect.BeanProperty
    openId:URL,
    credentials:Map[String,String]
  ) extends OIdUserCreds {
    override val name = email

    override def equals( in:Any ):Boolean = in match {
      case null => false
      case other:OIdUserCreds => (email == other.email) && (openId == other.openId)
      case _ => false
    }
  }
}

class OIdUserBuilder extends OIdUserCreds.Builder {
  override def build:OIdUserCreds = {
    this.validate()
    OIdUserBuilder.SimpleOIdUser( email, openId, Map( "email" -> email, "openId" -> openId.toString ) )
  }
}
