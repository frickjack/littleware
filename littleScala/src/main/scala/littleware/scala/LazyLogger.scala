/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.scala

import java.util.logging.Logger

/**
 * Pointless little logger factory - little less typing
 */
object LazyLogger {
  def apply( theClass:Class[_] ):Logger = Logger.getLogger( theClass.getName )
  def apply( className:String ):Logger = Logger.getLogger( className )
}
