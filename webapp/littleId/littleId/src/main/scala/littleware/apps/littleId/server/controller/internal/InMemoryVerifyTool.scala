/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.controller.internal

import com.google.inject.{Inject,Provider}
import littleware.apps.littleId
import littleware.base.cache.Cache
import littleId.server.{controller,model}
import littleware.scala.LazyLogger

class InMemoryVerifyTool @Inject() (
  cacheBuilder:Provider[Cache.Builder]
  ) extends controller.AuthVerifyTool {
  private val log = LazyLogger( getClass )

  private val cache:Cache[String,littleId.OIdUserCreds] = cacheBuilder.get.maxAgeSecs( 300 ).maxSize( 20000 ).build()

  override def cacheCreds( secret:String, creds:littleId.OIdUserCreds ):Unit = {
    log.fine( "Caching secret: " + secret )
    cache.put( secret, creds )
  }
  override def verifyCreds( secret:String, checkCreds:littleId.OIdUserCreds ):Boolean = {
    Option( cache.remove( secret ) ).map(
      (savedCreds) => {
        if( (savedCreds.email == checkCreds.email) && (savedCreds.openId == checkCreds.openId) ) {
          true
        } else {
          log.fine( "Credentials do not match: " + savedCreds + " ?= " + checkCreds )
          false
        }
      }
    ).getOrElse( {
        log.fine( "No entry for secret: " + secret )
        false
    }
        )
  }
}
