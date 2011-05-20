/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.test;

import com.google.inject.Inject;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.web.servlet.ThumbServlet;
import org.easymock.EasyMock;

public class ThumbServletTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(ThumbServletTester.class.getName());
    private final ThumbServlet servlet;

    @Inject
    public ThumbServletTester(ThumbServlet thumbServlet) {
        this.servlet = thumbServlet;
        setName("testThumbServlet");
    }

    public void testThumbServlet() {
        try {
            final HttpServletRequest mockRequest = EasyMock.createMock(HttpServletRequest.class);
            final HttpServletResponse mockResponse = EasyMock.createMock(HttpServletResponse.class);

            EasyMock.expect(mockRequest.getParameter("path")).andReturn(getTestHome());
            EasyMock.expect( mockRequest.getParameter( "res" ) ).andReturn( null );
            mockResponse.setContentType("image/png");
            EasyMock.expect(mockResponse.getOutputStream()).andReturn(
                    new ServletOutputStream() {
                        @Override
                        public void write(int b) throws IOException {
                        }
                    });
            EasyMock.replay( mockRequest );
            EasyMock.replay( mockResponse );
            servlet.doGetOrPost(mockRequest, mockResponse);
            EasyMock.verify( mockRequest );
            EasyMock.verify( mockResponse );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        }
    }
}
