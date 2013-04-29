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
 * Just defines a few constants.
 * per-client littleware environment.
 * An HttpSessionListener like littleware.web.servlet.LoginHandler
 * is a good place to bootstrap and shutdown littleware
 * in a web environment.
 */
public class WebBootstrap {
    /** Bean names in session */
    public static String littleGuice = "guiceBean";

    
    /**
     * Little helper boots the given bootstrap builder,
     * and sets a GuiceBean for an unauthenticated session in the 
     * servlet context littleGues attribute.
     */
    public static GuiceBean bootstrap( AppBootstrap.AppBuilder bootBuilder, ServletContext context ) {
        final AppBootstrap boot = bootBuilder.profile(AppBootstrap.AppProfile.WebApp).build();
        final GuiceBean gbean = boot.bootstrap( GuiceBean.class );
        context.setAttribute( littleGuice, gbean );
        return gbean;
    }
}
