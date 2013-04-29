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
import java.util.logging.Level
import littleId.server.{controller,model}
import java.util.logging.{Level,Logger}

class InMemoryVerifyTool @Inject() (
  cacheBuilder:Provider[Cache.Builder]
) extends controller.AuthVerifyTool {
  private val log = Logger.getLogger( getClass.getName )

  private val cache:Cache[String,littleId.common.model.OIdUserCreds] = cacheBuilder.get.maxAgeSecs( 300 ).maxSize( 20000 ).build()

  override def cacheCreds( secret:String, creds:littleId.common.model.OIdUserCreds ):Unit = {
    log.fine( "Caching secret: " + secret )
    cache.put( secret, creds )
  }
  
  override def verifyCreds( secret:String, checkCreds:Map[String,String] ):Boolean = {
    cache.remove( secret ) match {
      case null => false
      case savedCreds => {
          ( checkCreds.size > 0 ) && // "Must check at least one credential to verify" )
          ! (checkCreds.toSeq.exists( _ match {
                // find a credential that doesn't match
                case (key,value) => {
                    savedCreds.credentials.get( key ).map(
                      (cacheValue) => {
                        (cacheValue != value) match {
                          case true => {
                              log.log( Level.FINE, "{0} credentials do not match: {1} != {2}",
                                      Array[Object]( key, value, cacheValue )
                              )
                              true
                          }
                          case _ => false
                        }
                      }
                    ).getOrElse(
                      {
                        log.log( Level.FINE, "Could not match credential: " +key )
                        true 
                      }
                    )
                  }
              }
            ))
        }
    }
  }
}
