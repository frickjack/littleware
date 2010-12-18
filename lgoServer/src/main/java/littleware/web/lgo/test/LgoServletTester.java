/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.lgo.test;

import com.google.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.base.Whatever;
import littleware.test.LittleTest;
import littleware.web.lgo.LgoServlet;
import org.easymock.EasyMock;

public class LgoServletTester extends LittleTest {
    private static final Logger log = Logger.getLogger( LgoServletTester.class.getName() );
    private final LgoServlet servlet;

    @Inject
    public LgoServletTester( LgoServlet servlet ) {
        this.servlet = servlet;
        setName( "testLgoServlet" );
    }

    public void testLgoServlet() {
        try {
            final HttpServletRequest mockRequest = EasyMock.createMock(HttpServletRequest.class);
            final HttpServletResponse mockResponse = EasyMock.createMock(HttpServletResponse.class);
            final ByteArrayOutputStream data = new ByteArrayOutputStream( 1024*100 );
            final PrintWriter servletWriter = new PrintWriter( data );
            EasyMock.expect(mockRequest.getParameterNames()).andReturn( Collections.enumeration( Collections.singletonList("path" ) ) );
            EasyMock.expect( mockRequest.getParameterValues("path" ) ).andReturn( Collections.singletonList( "/" + getTestHome() ).toArray( new String[1] ) );
            EasyMock.expect( mockRequest.getPathInfo() ).andReturn( "/n9n/lgo/get" );
            mockResponse.setContentType( "text/html" );
            mockResponse.setStatus(HttpServletResponse.SC_OK);
            EasyMock.expect(mockResponse.getWriter()).andReturn( servletWriter ).anyTimes();
            EasyMock.replay( mockRequest );
            EasyMock.replay( mockResponse );
            servlet.doGetOrPost( mockRequest, mockResponse );
            servletWriter.flush();
            EasyMock.verify( mockRequest );
            EasyMock.verify( mockResponse );
            final String regex = "\"name\":\"" + getTestHome() + "\"";
            final Pattern pattern = Pattern.compile( regex );
            final String  result = data.toString( Whatever.UTF8.toString() );
            log.log( Level.INFO, "LgoServlet get /" + getTestHome() + ": " + result );
            assertTrue( "Got json asset result " + regex + " =~ " + result,
                    pattern.matcher( result ).find()
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Test failed: " + ex );
        }
    }
}
