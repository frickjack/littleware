/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.web.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.*;
import javax.servlet.*;
import littleware.security.auth.SessionUtil;

/**
 * Servlet bootstraps littleware appserver objects.
 * Starts up SessionManager server and registers with JNDI.
 * Initializes GUICE injector, and registers it into
 * application scope (TODO).
 */
public class LittleBootstrap extends HttpServlet {
private static final Logger olog = Logger.getLogger( LittleBootstrap.class.getName() );

    private Exception oex_error = null;
    
    /**
     * Start up the littleware backend
     * 
     * @param config parameters from web.xml and the appserver
     * @throws javax.servlet.ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
	super.init(config);

        try {
            SessionUtil.get().getSessionManager ();
        } catch ( Exception ex ) {
            oex_error = ex;
            olog.log( Level.SEVERE, "Failed to bootstrap Littleware SessionManager", ex );
            throw new ServletException( "Failed to bootstrap", ex );
        }
    }    
        
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res)
            	throws ServletException, IOException {
        PrintWriter writer = res.getWriter ();
        
        writer.print( "<html>\n<head><title>Bootstrap Status</title></head>\n<body>");
        writer.print( "<h3>Bootstrap Status</h3>\n" );
        if ( oex_error == null ) {
            writer.append( "<p> Bootstrap ok.</p>\n" );
        } else {
            writer.append( "<p> Bootstrap failed, caught: " + oex_error.getMessage () +
                    ", see logs for details.</p>\n"
                    );
        }
        writer.append( "</body></html>\n" );
    }
    
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res)
	throws ServletException, IOException {
        doPost( req,res );
    }
}
