/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.common.model

import java.net.URL
import littleware.scala.PropertyBuilder
import org.joda.{time => jtime}

/**
 * UserCredentials guaranteed to have e-mail and open-id
 */
case class OIdUserCreds private[model] (
  email:String,
  openId:URL,
  /** The date these creds were created - services may decide how long to accept these credentials */
  dateCreated:jtime.DateTime
) extends UserCreds {
    val name:String = email
    val credentials:Map[String,String] = Map( "email" -> email, "openId" -> openId.toString )
    
  override def equals( other:Any ):Boolean =
    (null != other) && (other match {
      case OIdUserCreds(email,openId,date) => this.email == email && this.openId == openId &&
        math.abs( this.dateCreated.getMillis - date.getMillis ) < 10000
    })
  
}

object OIdUserCreds {
  private val rxEmail = """^[\w\.-]+\@[\w\.]+$""".r
  
  
  class Builder extends PropertyBuilder {
    import PropertyBuilder._
    val email = new NotNullProperty[String]( 
      null, sanityCheck( nullCheck, "email has valid syntax" -> (v => rxEmail.findFirstIn(v).nonEmpty) )
    ).name( "email" )

    val openId = new NotNullProperty[URL]().name( "openId" )
    
    val dateCreated = new NotNullProperty[jtime.DateTime]( jtime.DateTime.now() ).name( "dateCreated" )

    def build():OIdUserCreds = {
      this.assertSanity
      OIdUserCreds( email(), openId(), dateCreated() )
    }
  }
}