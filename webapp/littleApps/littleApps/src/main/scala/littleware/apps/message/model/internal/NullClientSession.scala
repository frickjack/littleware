/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model
package internal

import org.joda.{time => jtime}


case class NullClientSession(
  sessionId:java.util.UUID,
  dateCreated:jtime.DateTime,
  dateExpires:jtime.DateTime
) extends ClientSession {}
