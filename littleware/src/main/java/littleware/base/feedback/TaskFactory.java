/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.feedback;

import com.google.common.base.Function;

/**
 * Factory launches a given function on an internal
 * executor service with feedback input associated
 * with the appropriate UI element.
 */
public interface TaskFactory {
    public <T> TaskHandle<T> launchTask( Function<Feedback,T> func );
}
