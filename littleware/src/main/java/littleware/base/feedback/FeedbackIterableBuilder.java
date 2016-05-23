/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.feedback;

import littleware.base.feedback.internal.SimpleIterable;
import com.google.inject.ImplementedBy;
import java.util.Collection;

/**
 * Setup Iterable wrapper around collection where the
 * iterable's iterator advances the given Feedback progress bar
 */
@ImplementedBy(SimpleIterable.class)
public interface FeedbackIterableBuilder {
    /**
     * Return iterable connected to Guice-injected Feedback implementation
     */
    public <T> Iterable<T> build( Collection<T> wrap );
    /**
     * Client must provide size of collection iterable references for
     * feedback to provide useful info
     */
    public <T> Iterable<T> build( Iterable<T> wrap, int size );

    public <T> Iterable<T> build( Collection<T> wrap, Feedback fbOverride );
    public <T> Iterable<T> build( Iterable<T> wrap, int size, Feedback fbOverride );
}
