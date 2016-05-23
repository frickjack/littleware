/*
 * Copyright 2013 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.internal;

import com.google.inject.Injector;
import littleware.base.Option;
import littleware.base.Options;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.LittleBootstrap;


/**
 * Internal implementation of SimpleAppFactory - interacts with AbstractLittleBootstrap
 * to try to maintain singleton littleware runtime
 */
public class SimpleAppFactory implements LittleBootstrap.Factory {

    private Option<Injector> optActive = Options.empty();
    
    private SimpleAppFactory(){}

    /**
     * Internal method - set null on shutdown to clear active app
     * @param injector application-scoped injector
     */
    public void setActiveRuntime(Injector value) {
        this.optActive = Options.some(value);
    }

    @Override
    public Option<LittleBootstrap> getActiveRuntime() {
        if (optActive.isEmpty()) {
            return Options.empty();
        }
        return Options.some(lookup(LittleBootstrap.class));
    }

    @Override
    public <T> T lookup(Class<T> clazz) {
        if (optActive.isEmpty()) {
            return AppBootstrap.appProvider.get().build().bootstrap(clazz);
        }
        return optActive.get().getInstance(clazz);
    }


    private static final SimpleAppFactory singleton = new SimpleAppFactory();
    
    public static SimpleAppFactory getSingleton() { return singleton; }
}
