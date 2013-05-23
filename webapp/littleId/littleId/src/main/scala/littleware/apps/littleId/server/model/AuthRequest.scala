/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.model


import java.util.UUID
import littleware.apps.littleId.common.model.OIdProvider
import littleware.scala.PropertyBuilder
import org.joda.{time => jtime}

/**
 * User-supplied data for open-id authentication
 */
case class AuthRequest private[model](
  id:UUID, 
  openIdProvider:OIdProvider.Value,
  dateTime:jtime.DateTime
 ) {}
  

object AuthRequest {
  class Builder extends PropertyBuilder {
    val id = new NotNullProperty[UUID]( UUID.randomUUID ).name( "id" )
    val openIdProvider = new NotNullProperty[OIdProvider.Value]( OIdProvider.Google ).name( "openIdProvider" )
    val dateTime = new NotNullProperty[jtime.DateTime]( jtime.DateTime.now() ).name( "dateTime" )
    //val replyToURL = new NotNullProperty[java.net.URL]().name( "replyToURL" )
    
    def build():AuthRequest = {
      this.assertSanity
      AuthRequest( id(), openIdProvider(), dateTime() )
    }
  }
}
