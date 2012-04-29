/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.feedback;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Handle returned by TaskFactory
 */
public interface TaskHandle<T> {
    public Function<Feedback,T> getFunction();
    public ListenableFuture<T>  getFuture();
    public Feedback             getFeedback();
}
