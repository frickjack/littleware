/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model
package internal

case class NamePasswordCreds( name:String, password:String ) extends Credentials {}

object NamePasswordCreds {
  class Factory extends Credentials.Factory {
    override def namePasswordCreds( name:String, password:String ):Credentials =
      NamePasswordCreds( name, password )
  }
}