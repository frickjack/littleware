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
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.bootstrap.AppBootstrap;
import littleware.security.auth.SessionHelper;
import littleware.web.beans.GuiceBean;

/**
 * Specialization of ClientBootstrap sets up
 * per-client littleware environment.
 * An HttpSessionListener like littleware.web.servlet.LoginHandler
 * is a good place to bootstrap and shutdown littleware
 * in a web environment.
 */
public class WebBootstrap {
    /** Bean names in session */
    public static String littleGuice = "littleGuice";
    public static String littleBoot = "littleBoot";

    /**
     * Little helper function to setup a standard client-session environment
     * with a GuiceBean, whatever.
     */
    public static ClientBootstrap bootstrap( SessionHelper helper, HttpSession session ) {
        final ClientBootstrap boot = ClientBootstrap.clientProvider.get().profile(AppBootstrap.AppProfile.WebApp).build(
                ).helper(helper);
        final Injector injector = boot.bootstrap( Injector.class );
        session.setAttribute(littleGuice, new GuiceBean( injector ) );
        session.setAttribute(littleBoot, boot );
        return boot;
    }

    /**
     * Setup a littleware environment (guicebean, bootstrap) in application scope.
     * Bootstraps littleware, and registers GuiceBean and LittleBoot
     * with the ServletContext.
     */
    public static AppBootstrap bootstrap( ServletContext context ) {
        final AppBootstrap boot = AppBootstrap.appProvider.get().profile(AppBootstrap.AppProfile.WebApp).build();
        final Injector injector = boot.bootstrap( Injector.class );
        context.setAttribute(littleGuice, new GuiceBean( injector ) );
        context.setAttribute(littleBoot, boot );
        return boot;
    }
}
