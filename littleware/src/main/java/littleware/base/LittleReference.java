package littleware.base;


import com.google.common.base.Supplier;
import java.util.Optional;
import java.util.function.Consumer;
import littleware.base.event.LittleBean;

/**
 * Reference to a (usually immutable) object where the
 * reference object might be changed by some underlying
 * engine (like a cache) notifying property-change listeners.
 * Basically a mutable Optional.
 * The underlying system may change the thing referenced
 * when a new version of the thing becomes available firing
 * a PropertyChangeEvent.
 */
public interface LittleReference<T> extends LittleBean, Iterable<T> {
    /**
     * Get an optional view of the current reference
     */
    public Optional<T>  asOptional();
    public boolean isPresent();
    public boolean isEmpty();
    public T orElse( T alt );
    public T orElseGet( Supplier<? extends T> supplier );
    public void ifPresent( Consumer<? super T> consumer );
    
    /**
     * Get the value if set, otherwise throw NoSuchElementException
     */
    public T get ();
}
