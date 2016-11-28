package littleware.security.auth.client.internal;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import littleware.base.event.LittleListener;
import littleware.base.event.helper.SimpleLittleTool;
import littleware.security.auth.client.KeyChain;


public class SimpleKeyChain implements KeyChain {
    private final String defaultHost;
    private final SimpleLittleTool support = new SimpleLittleTool( this );
    private final Map<String,UUID> keyMap = new MapMaker().makeMap();


    @Inject
    public SimpleKeyChain( @Named( "littleware.rmi_host" ) String defaultHost ) {
        this.defaultHost = defaultHost;
    }
    
    @Override
    public Optional<UUID> getDefaultSessionId() {
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

    final Optional<UUID> empty = Optional.empty();

    
    public Optional<UUID> getHostSessionId(String host) {
        UUID id = keyMap.get( host );
        if( null == id ) {
            support.fireLittleEvent( new KeyChain.LoginRequestedEvent( this, "bla" ));
        }
        id = keyMap.get(host);
        if ( null == id ) {
            return empty;
        }
        return Optional.ofNullable( id );
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
