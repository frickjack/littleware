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

import com.google.inject.ImplementedBy;
import javax.servlet.http.HttpServlet;

/**
 * Just a little utility that wraps a LittleServlet
 * with an HttpServlet that delegates doGet and doPost.
 * Suitable for embedded environment where every request
 * is associated with the same littleware session.
 */
@ImplementedBy(SimpleEmbeddedBuilder.class)
public interface EmbeddedServletBuilder {
    public HttpServlet build( LittleServlet littleServlet );
}
