/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package controller


trait MessageListener {
  val id:java.util.UUID
  def messageArrival( event:model.MessageEvent, fb:littleware.base.feedback.Feedback ):Unit
}
