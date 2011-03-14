/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.internal

import java.net.URL
import littleware.apps.littleId.OIdUserCreds

object SimpleOIdUserBuilder {
  case class SimpleOIdUser( override val email:String,
                           override val openId:URL
  ) extends OIdUserCreds {}
}

class SimpleOIdUserBuilder extends OIdUserCreds.Builder {
  override def build:OIdUserCreds = {
    this.validate()
    new SimpleOIdUserBuilder.SimpleOIdUser( email, openId )
  }
}
