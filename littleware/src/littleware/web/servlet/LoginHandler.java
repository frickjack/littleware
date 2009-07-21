/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.servlet;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Combination servlet and HttpSessionListener.
 * When a session is created injects a SessionMean
 * with an empty SessionHelper or a "guest" session helper
 * depending on configuration.
 * If the servlet receives a request, then it looks
 * for:
 *        username, password, sessionId, urlSuccess, urlFailure
 * parameters.  The servlet attempts to authenticate
 * a new session via sessionId, username, and password as
 * appropriate.  If login fails, then bounces the user
 * to urlFailure.  If login succeeds, then if the HttpSession
 * isNew is false, then create a new session.
 * Once a new session is established, then
 * configure a new SessionBean and bootstrap environment
 * for the session.
 * Finally, at session shutdown time do any littleware cleanup
 * necessary.
 */
public class LoginHandler extends HttpServlet implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException {

    }

}
