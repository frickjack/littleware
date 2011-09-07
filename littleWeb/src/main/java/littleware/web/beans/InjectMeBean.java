/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.beans;

import javax.faces.bean.ManagedProperty;



/**
 * Provides a default get/setGuiceBean implementation
 * where setGuiceBean does an 'injectMembers'
 * call the first time called.
 * Sort of a visitor pattern.
 */
public abstract class InjectMeBean {
    @ManagedProperty(value="#{littleGuice}")
    private transient GuiceBean  guiceBean = null;

    public GuiceBean  getGuiceBean () { return guiceBean; }
    /**
     * Invokes gbean.injectMembers( this ) first time called
     *
     * @throws IllegalStateException if called multiple times
     */
    public void setGuiceBean ( GuiceBean gbean ) {
        if ( null != guiceBean ) {
            throw new IllegalStateException ( "Bean already injected" );
        }
        gbean.injectMembers( this );
        guiceBean = gbean;
    }

}
