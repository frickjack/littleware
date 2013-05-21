/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId
package client
package controller
package internal

import com.google.gson
import com.google.inject.Inject
import com.google.inject.name.Named
import com.google.common.{io => gio}
import java.io
import java.net.{URL,URLEncoder,URLConnection, HttpURLConnection}
import java.util.logging.{Level,Logger}
import littleware.base.Whatever


class HttpVerifyTool @Inject()( 
  @Named( "littleId.verfiyURL" ) verifyURL:URL,
  gsonTool:gson.Gson
) extends controller.VerifyTool {
  private val log = Logger.getLogger( getClass.getName )
  
  def verify( secret:String ):Option[common.model.OIdUserCreds] = {
    val postData:String = 
      (new StringBuilder).append( "secret=" ).append( URLEncoder.encode( secret, Whatever.UTF8.toString )).toString

    val (verifyResponse:String,httpCode:Int) = {
      val conn:HttpURLConnection = verifyURL.openConnection.asInstanceOf[HttpURLConnection]
      conn.setDoOutput( true )
      val writer = new io.OutputStreamWriter( conn.getOutputStream(), Whatever.UTF8 )
      try {
        writer.write( postData )
        writer.flush
        val reader = new io.BufferedReader( new io.InputStreamReader( conn.getInputStream, Whatever.UTF8 ))
        try {
          (gio.CharStreams.toString( reader ), conn.getResponseCode)
        } finally reader.close
      } finally writer.close
      
    }
    
    log.log( Level.FINE, "Verify request to {0} got response http-status:{1} - {2}", 
            Array[Object]( verifyURL, new Integer( httpCode ), verifyResponse ) 
    )
    
    if( httpCode >= 200 && httpCode <= 300 ) {
      Some( gsonTool.fromJson( verifyResponse, classOf[common.model.OIdUserCreds]) )
    } else None
  }
}
