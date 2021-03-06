/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message.model

trait Credentials {
}

object Credentials {
  case class NamePassword( name:String, password:String ) extends Credentials {}
  case class LittleId ( email:String, secret:String ) extends Credentials {}
  case class SessionId ( sessionId:java.util.UUID ) extends Credentials {}
}
