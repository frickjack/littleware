/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.client.controller.internal

import com.google.inject.Inject
import com.google.inject.name.Named
import com.google.common.{io => gio}
import java.io
import java.net.{URL,URLEncoder,URLConnection}
import littleware.apps.littleId
import littleId.client.controller
import littleware.scala.LazyLogger
import littleware.base.Whatever


class HttpVerifyTool @Inject()( @Named( "littleId.verfiyURL" ) verifyURL:URL ) extends controller.VerifyTool {
  private val log = LazyLogger( getClass )
  
  def verify( secret:String, creds:Map[String,String] ):Boolean = {
    val postData:String = (
      (new StringBuilder).append( "secret=" ).append( URLEncoder.encode( secret, Whatever.UTF8.toString )) /:
      creds.toSeq
    )( (sb,entry) => entry match {
        case (key,value) => sb.append( "&" 
          ).append( key ).append( "="
          ).append( URLEncoder.encode( value, Whatever.UTF8.toString ))
      }
    ).toString

    val verifyResponse:String = {
      val conn:URLConnection = verifyURL.openConnection
      conn.setDoOutput( true )
      val writer = new io.OutputStreamWriter( conn.getOutputStream(), Whatever.UTF8 )
      try {
        writer.write( postData )
        writer.flush
        val reader = new io.BufferedReader( new io.InputStreamReader( conn.getInputStream, Whatever.UTF8 ))
        try {
          gio.CharStreams.toString( reader )
        } finally reader.close
      } finally writer.close
    }
    log.fine( "Verify request to " + verifyURL + " got response: " + verifyResponse )
    verifyResponse.replaceAll( "\\s+", "" ).indexOf( "<verify>true</verify>" ) >= 0
  }
}
