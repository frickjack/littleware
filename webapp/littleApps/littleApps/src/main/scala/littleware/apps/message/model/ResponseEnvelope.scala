/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model

import org.joda.{time => jtime}

/**
 * Response extended with its index in the response-message stream,
 * and the MessageHandel the response is associated with.
 */
case class ResponseEnvelope(
  transaction:Long,
  datePosted:jtime.DateTime,
  handle:MessageHandle,
  response:Response
  ) {}

