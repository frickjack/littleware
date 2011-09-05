/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.auth.client;

import java.util.UUID;
import littleware.base.Option;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleEventSource;

/**
 * Singleton registry tracks the sessionId associated with
 * a session.
 */
public interface KeyChain extends LittleEventSource {

    /**
     * Event fired by KeyChain on attempt to access an
     * uninitialized key.
     */
    public static class LoginRequestedEvent extends LittleEvent {
        private final String host;
        
        public LoginRequestedEvent( KeyChain source, String host ) {
            super( source );
            this.host = host;
        }

        /**
         * Get the host of the server the session is trying to access
         *
         * @return
         */
        public String  getHost() {
            return host;
        }

        @Override
        public KeyChain getSource() {
            return (KeyChain) super.getSource();
        }
    }

    /**
     * Return the sessionId active with the default littleware server.
     * Fires LoginRequestedEvent if not yet set, then checks again
     * before returning empty
     */
    public Option<UUID>  getDefaultSessionId();
    public void          setDefaultSessionId( UUID value );
    public String        getDefaultHost();

    /**
     * Return the sessionId active with the littleware server at the given host.
     * Fires LoginRequestedEvent if not yet set, then checks again
     * before returning empty
     *
     * Do not yet implement multi-host support ...
     * 
    public Option<UUID> getHostSessionId( String host );
    public void setHostSessionId( String host, UUID value );
     */
}
