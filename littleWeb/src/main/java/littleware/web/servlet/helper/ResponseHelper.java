/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.helper;

import com.google.gson.Gson;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.servlet.http.HttpServletResponse;
import littleware.base.Whatever;

/**
 * Just provide some useful utilities
 */
public class ResponseHelper {
  private final Gson gsonTool = new Gson();
  
  /**
   * Shortcut assembles a UTF8 BufferedWriter to resp.getOutputStream
   */
  public BufferedWriter utf8Writer( HttpServletResponse resp ) throws IOException {
    return new BufferedWriter( new OutputStreamWriter( resp.getOutputStream(), Whatever.UTF8 ));
  }
  
  
  /**
   * Shortcut sets response status, content-type, writes
   * js to output in UTF8, and flushes output
   * 
   * @param resp
   * @param js 
   */
  public void write( HttpServletResponse resp, JsonResponse jsr ) throws IOException {
    resp.setContentType( "application/json" );
    resp.setStatus( jsr.getStatus() );
    utf8Writer( resp ).append( gsonTool.toJson( jsr.getContent() )).flush();
  }
}
