/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.base.event;

/**
 * Source of LittleEvents
 */
public interface LittleEventSource {

    public void addLittleListener(LittleListener listener);

    public void removeLittleListener(LittleListener listener);

}
