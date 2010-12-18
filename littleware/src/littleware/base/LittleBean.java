/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

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
