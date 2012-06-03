/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model

trait MessageHandle {
  val id:java.util.UUID
}

object MessageHandle {
  def apply( id:java.util.UUID ):MessageHandle = internal.SimpleMessageHandle( id )
}

