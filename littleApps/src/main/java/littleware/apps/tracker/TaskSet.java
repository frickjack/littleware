/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.tracker;

import com.google.inject.ImplementedBy;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import littleware.base.feedback.Feedback;


/**
 * Iterable lazy-load task result
 */
public interface TaskSet extends Iterable<Task> {
    /**
     * The number of tasks in the set - so
     * can tie iterable to a progress feedback.
     */
    public int getSize();

    /**
     * Build an iterator tied to the given feedback object -
     * advances feedback progress on each call to next.
     */
    public Iterator<Task> iterator( Feedback feedback );

    @ImplementedBy(SimpleTaskSetBuilder.class)
    public interface IdSetBuilder {
        public TaskSet build( Collection<UUID> idSet );
    }
}
