/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.servlet;

import com.google.inject.Injector;
import javax.servlet.ServletContext;
import littleware.bootstrap.AppBootstrap;
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
    public static String littleBoot = "littleBoot";
    public static String littleGuice = "guiceBean";
    public static String littleInjector = "injector";
    
    /**
     * Setup a littleware environment (guicebean, bootstrap) in application scope.
     * Bootstraps littleware, and registers GuiceBean and LittleBoot
     * with the ServletContext.
     */
    public static AppBootstrap bootstrap( ServletContext context ) {
        final AppBootstrap boot = AppBootstrap.appProvider.get().profile(AppBootstrap.AppProfile.WebApp).build();
        final GuiceBean gbean = boot.bootstrap( GuiceBean.class );
        context.setAttribute( littleGuice, gbean );
        context.setAttribute(littleBoot, boot );
        context.setAttribute( littleInjector, gbean.getInjector().get().getInjector() );
        return boot;
    }
}
