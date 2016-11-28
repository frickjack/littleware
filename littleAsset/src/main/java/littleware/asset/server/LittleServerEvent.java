package littleware.asset.server;

import java.security.AccessController;
import java.util.EventObject;
import java.util.Optional;
import java.util.UUID;
import javax.security.auth.Subject;
import littleware.asset.Asset;


/**
 * Base class for generic server-side events.
 * The LittleServerListeners that register with the ServerEventBus
 * receive LittleServerEvents after a transaction that manipulates an
 * asset completes on a separate thread.
 * The listener process runs as the user that generates the event.
 */
public class LittleServerEvent extends EventObject {
    private static final long serialVersionUID = 8882466619235817165L;
    private final UUID  id = UUID.randomUUID();
    private final Asset assetSource;
    private final Subject subject;
    private final Optional<LittleServerEvent> maybeParent;

    public LittleServerEvent( Asset source ) {
        super( source );
        this.assetSource = source;
        this.subject = Subject.getSubject( AccessController.getContext() );
        this.maybeParent = Optional.empty();
    }

    public LittleServerEvent( Asset source, LittleServerEvent parentEvent ) {
        super( source );
        this.assetSource = source;
        this.subject = Subject.getSubject( AccessController.getContext() );
        this.maybeParent = Optional.ofNullable( parentEvent );
    }


    @Override
    public Asset getSource() { return assetSource; }
    public Subject getSubject() { return subject; }
    /**
     * Get event id - helps avoid loops
     */
    public UUID    getId() { return id; }
    public Optional<LittleServerEvent>  getParent() { return maybeParent; }
}
