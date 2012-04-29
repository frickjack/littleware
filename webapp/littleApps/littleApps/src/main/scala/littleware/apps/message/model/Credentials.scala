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
  /**
   * Factory for building different types of credentials.
   * A particular MessageClient may only accept some particular
   * types of credential
   */
  trait Factory {
    def namePasswordCreds( name:String, password:String ):Credentials
  }
}
