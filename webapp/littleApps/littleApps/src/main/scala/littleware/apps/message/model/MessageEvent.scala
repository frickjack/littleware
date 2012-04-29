/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model

/**
 * Event bundles together a message instance
 * with its runtime handle and client session
 */
case class MessageEvent( 
  message:Message, 
  handle:MessageHandle, 
  session:ClientSession 
) {
}
