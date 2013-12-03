/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interface for servlet delegate tied to a particular
 * user's littleware session via Guice injection by the
 * LittleDispatcher servlet.
 * A LittleServlet may be used directly in an embedded
 * environment too.
 * 
 * @see littleware.web.servlet.LittleDispatcher
 */
public interface LittleServlet {
    /**
     * Universal handler for GET, POST, PUT, and DELETE too.
     */
    void  doGetOrPostOrPut( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException;
        
}
