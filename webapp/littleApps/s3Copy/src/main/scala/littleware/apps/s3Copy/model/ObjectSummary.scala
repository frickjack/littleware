/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.s3Copy.model

import java.net.URI
import org.joda.{time => jtime}


/**
 * Metadata subset for storage object
 */
case class ObjectSummary private[model](
  path:URI,
  md5Hex:String,
  sizeBytes:Long,
  lastModified:jtime.DateTime
  ) extends PathSummary {}


object ObjectSummary {
  class Builder extends littleware.scala.PropertyBuilder {
    val path = new NotNullProperty[URI]().name( "path" )
    val md5Hex = new NotNullProperty[String]().name( "md5Hex" )
    val sizeBytes = new Property[Long]( 0L, 
                                       (v:Long) => { if( v < 0 ) Seq( "Value must be >= 0" ) else Nil }
          ).name( "sizeBytes" )
    val lastModified = new NotNullProperty[jtime.DateTime]().name( "lastModified" )
    
    def build():ObjectSummary = {
      this.assertSanity()
      new ObjectSummary( path(), md5Hex(), sizeBytes(), lastModified() )
    }
  }
}