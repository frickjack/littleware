package littleware.base;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import littleware.base.event.LittleBean;

/**
 * Utility base class for Option and LittleReference implementations
 */
public abstract class AbstractReference<T> implements java.io.Serializable, Iterable<T>, LittleBean {
    private String   errorMessage = null;
    private T        value = null;
    private final PropertyChangeSupport propertySupport = new PropertyChangeSupport( this );

    public boolean isPresent() { return (null != value); }
    public boolean isEmpty() { return ! isPresent(); }

    /** Construct an unset Maybe */
    protected AbstractReference () {}
    /** Construct an isSet Maybe */
    protected AbstractReference ( T val ) {
        value = val;
    }


    /**
     * Allow subtypes to throw property change events
     */
    protected PropertyChangeSupport getPropertySupport() {
        return propertySupport;
    }

    public T getOr( T alt ) {
        if ( isPresent() ) {
            return value;
        } else {
            return alt;
        }
    }


    public T getOrCall( Callable<T> call ) throws Exception {
        if ( isPresent() ) {
            return value;
        } else {
            return call.call ();
        }
    }

    public boolean nonEmpty() { return isPresent(); }

    public T get () {
        if ( ! isPresent() ) {
            if ( (null != errorMessage) && (errorMessage.length() > 0) ) {
                throw new NoSuchElementException( errorMessage );
            }
            throw new NoSuchElementException();
        }
        return value;
    }

    public T getOrThrow( RuntimeException ex ) {
      if ( isEmpty() ) { throw ex; } else { return value; }
    }
    
    public T getOrThrow( Exception ex ) throws Exception {
      if ( isEmpty() ) { throw ex; } else { return value; }
    }

    
    public final T getRef() {
        return get();
    }

    public void clear() {
        final T old = value;
        this.value = null;
        propertySupport.firePropertyChange( new PropertyChangeEvent( this, "ref", old, value ) );
    }

    /**
     * Allow mutable subtypes
     */
    protected void setThing( T value ) {
        this.value = value;
    }

    @Override
    public boolean equals( final Object other ) {
        if ( other instanceof LittleReference ) {
            final LittleReference<?> maybe = (LittleReference<?>) other;
            return (isPresent() == maybe.isPresent()) &&
                    (isPresent() ? get().equals( maybe.get() ) : true);
        } else {
            return isPresent() && get().equals( other );
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.isPresent() ? 1 : 0);
        hash = 37 * hash + (this.value != null ? this.value.hashCode() : 0);
        return hash;
    }

    /**
     * Error message property
     * attached to NoSuchElementException on get() call
     * against an empty option.  May return null.
     */
    protected String getErrorMessage() {
        return errorMessage;
    }
    protected final void setError( String value ) {
        putError( value );
    }
    protected AbstractReference putError( String value ) {
        errorMessage = value;
        return this;
    }

    @Override
    public String toString () {
        return isPresent() ? get().toString() : "null";
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
          int nextCount = 0;

            @Override
            public boolean hasNext() {
                return (0 == nextCount) && AbstractReference.this.isPresent();
            }

            @Override
            public T next() {
                if( hasNext() ) {
                    nextCount++;
                    return AbstractReference.this.get();
                }
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported.");
            }
        };
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertySupport.removePropertyChangeListener(listener);
    }

}
