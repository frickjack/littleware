/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.auth.client.internal;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.UUID;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.event.LittleListener;
import littleware.base.event.helper.SimpleLittleTool;
import littleware.security.auth.client.KeyChain;


public class SimpleKeyChain implements KeyChain {
    private final String defaultHost;
    private final SimpleLittleTool support = new SimpleLittleTool( this );
    private final Map<String,UUID> keyMap = new MapMaker().makeMap();

    private static  final String defaultRemoteHost;
    static {
        try {
            defaultRemoteHost = littleware.base.PropertiesLoader.get().loadProperties().getProperty( "littleware.rmi_host", "localhost" );
        } catch ( java.io.IOException ex ) {
            throw new littleware.base.AssertionFailedException( "Failed accessing littleware.properties", ex );
        }
    }
    
    public SimpleKeyChain( String defaultHost ) {
        this.defaultHost = defaultHost;
    }
    
    /**
     * Sets default remote host to littleware.rmi_host property in littleware.properties
     */
    public SimpleKeyChain() {
        this( defaultRemoteHost );
    }

    @Override
    public Option<UUID> getDefaultSessionId() {
        return getHostSessionId( defaultHost );
    }

    @Override
    public void setDefaultSessionId(UUID value) {
        setHostSessionId( defaultHost, value );
    }

    @Override
    public String getDefaultHost() {
        return defaultHost;
    }

    final Option<UUID> empty = Maybe.empty();

    
    public Option<UUID> getHostSessionId(String host) {
        UUID id = keyMap.get( host );
        if( null == id ) {
            support.fireLittleEvent( new KeyChain.LoginRequestedEvent( this, "bla" ));
        }
        id = keyMap.get(host);
        if ( null == id ) {
            return empty;
        }
        return Maybe.something( id );
    }

    
    public void setHostSessionId(String host, UUID value) {
        final UUID old = keyMap.get( host );
        keyMap.put(host, value);
        if ( host.equals( defaultHost ) ) {
            support.firePropertyChange( "defaultSessionId", old, value);
        }
    }

    @Override
    public void addLittleListener(LittleListener ll) {
        support.addLittleListener(ll);
    }

    @Override
    public void removeLittleListener(LittleListener ll) {
        support.removeLittleListener(ll);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        support.addPropertyChangeListener( pl );
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        support.removePropertyChangeListener( pl );
    }

}
