package littleware.base.event;

import java.beans.PropertyChangeListener;

/**
 * Just a helpful interface for mixin
 */
public interface LittleBean {
    /**
     * Allow observers to listen for property changes
     *
     * @param listen_props listener that wants informed when a setter gets invoked on this object
     */
    public void addPropertyChangeListener( PropertyChangeListener listen_props );

    /**
     * Allow observers to stop listening for changes
     *
     * @param listen_props to stop sending events to
     */
    public void removePropertyChangeListener( PropertyChangeListener listen_props );

}
