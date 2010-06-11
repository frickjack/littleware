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

import com.google.inject.Singleton;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Singleton
public class SimpleEmbeddedBuilder implements EmbeddedServletBuilder {

    @Override
    public HttpServlet build( final LittleServlet littleServlet) {
        return new HttpServlet() {
            @Override
            public void doGet( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
                littleServlet.doGetOrPost( req, res );
            }

            @Override
            public void doPost( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
                littleServlet.doGetOrPost( req, res );
            }

        };
    }

}