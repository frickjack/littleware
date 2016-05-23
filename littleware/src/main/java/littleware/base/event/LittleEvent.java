/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.event;

import java.util.EventObject;

/**
 * Base class for events related to littleware
 */
public abstract class LittleEvent extends EventObject {
    private static final long serialVersionUID = 9126648263480500740L;

    /**
     * Setup the LittleEvent with proper source and operation
     * with a successful null result.
     *
     * @param source of the event
     */
    public LittleEvent(Object source) {
        super(source);
    }

    public <T extends LittleEvent> T narrow() {
        return (T) this;
    }
    public <T extends LittleEvent> T narrow( Class<T> narrowClass ) {
        return narrowClass.cast(this );
    }
}

