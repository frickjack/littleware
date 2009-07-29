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

import com.google.inject.Injector;
import javax.servlet.http.HttpSession;
import littleware.apps.client.ClientBootstrap;
import littleware.web.beans.GuiceBean;

/**
 * Specialization of ClientBootstrap sets up
 * per-client littleware environment.
 * An HttpSessionListener like littleware.web.servlet.LoginHandler
 * is a good place to bootstrap and shutdown littleware
 * in a web environment.
 */
public class WebBootstrap extends ClientBootstrap {
    private final HttpSession session;

    public HttpSession getSession() {
        return session;
    }

    /** 
     * Inject the session to bootstrap into.
     * 
     * @param session
     */
    public WebBootstrap( HttpSession session ) {
        this.session = session;
    }



    /**
     * Specialization runs super.bootstrap(), then
     * registers a GuiceBean
     * with the session with name "littleGuice",
     * and registers this bootstrap object
     * with the session as "littleBoot"
     * so it can be accessed at session shutdown time.
     */
    @Override
    public void bootstrap() {
        final Injector injector = super.bootstrapInternal();
        getSession().setAttribute("littleGuice", new GuiceBean( injector ) );
        getSession().setAttribute("littleBoot", this );
    }
}
