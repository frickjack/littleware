/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.client.internal;

import com.google.inject.Singleton;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.asset.client.spi.LittleServiceEvent;
import littleware.base.event.LittleListener;
import littleware.base.event.helper.SimpleLittleTool;

/**
 * Simple LittleServiceBus implementation
 */
@Singleton
public class SimpleServiceBus implements LittleServiceBus {
    private final SimpleLittleTool helper = new SimpleLittleTool( this );

    @Override
    public void fireEvent(LittleServiceEvent ev) {
        helper.fireLittleEvent(ev);
    }

    @Override
    public void addLittleListener(LittleListener ll) {
        helper.addLittleListener(ll);
    }

    @Override
    public void removeLittleListener(LittleListener ll) {
        helper.removeLittleListener(ll);
    }
}
