package littleware.base;


import java.util.concurrent.Callable;
import littleware.base.event.LittleBean;

/**
 * Reference to a (usually immutable) object where the
 * reference object might be changed by some underlying
 * engine (like a cache) notifying property-change listeners.
 * Basically a mutable Option.
 * The underlying system may change the thing referenced
 * when a new version of the thing becomes available firing
 * a PropertyChangeEvent.
 */
public interface LittleReference<T> extends LittleBean, Iterable<T> {
    public boolean isPresent();
    public boolean isEmpty();
    public boolean nonEmpty();
    public T getOr( T alt );
    public T getOrCall( Callable<T> call ) throws Exception;
    public T getOrThrow( RuntimeException ex );
    public T getOrThrow( Exception ex ) throws Exception;
    
    /**
     * Get the value if set, otherwise throw NoSuchElementException
     */
    public T get ();
    /**
     * Just calls get() - setup as Property to simplify access
     * from JSF/JSP expression language, etc.
     */
    public T getRef();

    /**
     * Update the thing referenced if value.getTimestamp > thing.getTimestamp,
     * or clear thing if value is null
     *
     * @return this
     */
    public <R extends LittleReference<T>> R updateRef( T value );
    /**
     * isSet becomes false
     */
    public void clear();
}
