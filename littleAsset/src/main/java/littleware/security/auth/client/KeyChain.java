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
import littleware.base.event.LittleEventSource;

/**
 *
 * @author pasquini
 */
public interface KeyChain extends LittleEventSource {

    /**
     * Return the sessionId active with the default littleware server.
     * Fires LoginRequestedEvent if not yet set, then checks again
     * before returning empty
     */
    public Option<UUID>  getDefaultSessionId();
    public void  setDefaultSessionId( UUID value );

    /**
     * Return the sessionId active with the littleware server at the given host.
     * Fires LoginRequestedEvent if not yet set, then checks again
     * before returning empty
     */
    public Option<UUID> getHostSessionId( String host );
    public void setHostSessionId( String host, UUID value );
}
