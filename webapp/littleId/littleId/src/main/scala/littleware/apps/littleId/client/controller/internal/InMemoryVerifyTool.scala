/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId
package client
package controller
package internal


import com.google.gson
import com.google.inject
import java.util.logging.Level
import java.util.logging.{Level,Logger}


class InMemoryVerifyTool @inject.Inject() (
  tokenFoundry:littleware.web.jwt.TokenFoundry,
  gsonTool:gson.Gson
) extends VerifyTool {
  private val log = Logger.getLogger( getClass.getName )

  
  override def verify( secret:String ):Option[common.model.OIdUserCreds] = 
    Some( tokenFoundry.verifyToken( secret ) ).filter( _.isSet ).map( 
      optJs => {
        val js:gson.JsonObject = optJs.get
        log.log( Level.FINE, "Attempting to parse creds: {0}", js )
        gsonTool.fromJson( js, classOf[common.model.OIdUserCreds] ) 
      }
   )
  
}
