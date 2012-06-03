/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package web.servlet


import com.google.gson
import com.google.inject
import java.io
import java.util.logging.{Level,Logger}
import javax.servlet
import javax.servlet.{http => hservlet}
import littleware.{web => littleweb}
import littleware.base.UUIDFactory
import littleware.base.Whatever.UTF8

/**
 * Simple servlet for testing the message client.
 * TODO: integrate with session and credentials system
 */
class MessageClientServlet extends hservlet.HttpServlet {
  private val log = Logger.getLogger( getClass.getName ) 
  private var tools:MessageClientServlet.Tools = null
  val jsonMimeType:String = "application/json"
  
  @inject.Inject()
  def injectMe( tools:MessageClientServlet.Tools ):Unit = {
    this.tools = tools
  }
  
  @throws(classOf[servlet.ServletException])
  override def init():Unit = try {
    //Option( getServletConfig.getInitParameter( "viewPath" ) ).map( (value) => { verifyResponsePage = value })
    val gbean:littleweb.beans.GuiceBean = getServletContext.getAttribute( littleweb.servlet.WebBootstrap.littleGuice ).asInstanceOf[littleweb.beans.GuiceBean]
    gbean.injectMembers(this)
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Initialization failed", ex )
        ex match {
          case _:servlet.ServletException => throw ex
          case _ => throw new servlet.ServletException( "Initialization failed", ex )
        }
      }
  }  

  
  
  @throws(classOf[servlet.ServletException])
  override def doPut( req:hservlet.HttpServletRequest, resp:hservlet.HttpServletResponse ):Unit = {
    assert( null != tools, "Servlet tools properly initialized" )
    val message:model.Message = {
      val reader = new io.InputStreamReader( req.getInputStream, UTF8 )
      try {
        tools.gsonTool.fromJson( reader, classOf[model.Message] )
      } finally reader.close
    }

    val response = new gson.JsonObject
    try {
      val handle = tools.messClient.postMessage( tools.session, message )
      response.addProperty( "status", "ok" )
      response.addProperty( "handle", handle.id.toString )
    } catch {
      case ex => {
          log.log( Level.WARNING, "Failed put", ex )
          response.addProperty( "status", "failed" )
          response.addProperty( "details", ex.getMessage )
      }
    }
    resp.setContentType( jsonMimeType )
    resp.getWriter.append( response.toString )
  }
  
  @throws(classOf[servlet.ServletException])
  override def doGet( req:hservlet.HttpServletRequest, resp:hservlet.HttpServletResponse ):Unit = {
    val handle:model.MessageHandle = {
      val handleStr:String = req.getRequestURI.replaceAll( "/+$", "" ).replaceAll( "^.+/", "" )
      model.MessageHandle( UUIDFactory.parseUUID( handleStr ))
    }
    val responseSeq = tools.messClient.checkResponse( tools.session, handle )
    throw new UnsupportedOperationException( "not yet implemented" )
  }
}

object MessageClientServlet {
  
  class Tools @inject.Inject() (
    val messClient:controller.MessageClient,
    val messFactory:inject.Provider[model.Message.Builder],
    credsFactory:model.Credentials.Factory,
    gsonFactory:inject.Provider[gson.Gson]
  ) {
    lazy val gsonTool:gson.Gson = gsonFactory.get
    val anonymous:model.Credentials = credsFactory.namePasswordCreds("anonymous", "" )
    lazy val session:model.ClientSession = messClient.login( anonymous )
  }
  
}
