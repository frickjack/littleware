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

import java.security.Principal

/**
 * E-mail based user id credentials
 */
trait UserCreds extends Principal with java.io.Serializable {
  val name:String
  override def getName = name
  val credentials:Map[String,String]
}


