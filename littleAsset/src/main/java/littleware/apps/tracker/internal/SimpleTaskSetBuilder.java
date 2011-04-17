/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.apps.tracker.Task;
import littleware.apps.tracker.TaskSet;
import littleware.asset.AssetSearchManager;
import littleware.base.LazyLoadException;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.FeedbackIterableBuilder;

public class SimpleTaskSetBuilder implements TaskSet.IdSetBuilder {
    private static final Logger log = Logger.getLogger( SimpleTaskSetBuilder.class.getName() );
    private final AssetSearchManager search;
    private final FeedbackIterableBuilder fbItBuilder;

    @Inject
    public SimpleTaskSetBuilder( AssetSearchManager search,
            FeedbackIterableBuilder fbItBuilder ) {
        this.search = search;
        this.fbItBuilder = fbItBuilder;
    }

    @Override
    public TaskSet build(Collection<UUID> idSet) {
        return new SimpleTaskSet( idSet, search, fbItBuilder );
    }

    private static class SimpleTaskSet implements TaskSet {
        private final Collection<UUID> idSet;
        private final AssetSearchManager search;
        private final FeedbackIterableBuilder fbItBuilder;

        private class LazyIterator implements Iterator<Task> {

            private int progress = 0;
            private final Iterator<UUID> iterator = idSet.iterator();


            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Task next() {
                final UUID id = iterator.next();
                try {
                    return search.getAsset( id ).get().narrow ();
                } catch ( Exception ex ) {
                    throw new LazyLoadException( "Failed to retrieve asset " + id, ex );
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }

        public SimpleTaskSet(Collection<UUID> idSet,
                AssetSearchManager search,
                FeedbackIterableBuilder fbItBuilder ) {
            this.idSet = ImmutableList.copyOf(idSet);
            this.search = search;
            this.fbItBuilder = fbItBuilder;
        }

        @Override
        public int getSize() {
            return idSet.size();
        }

        @Override
        public Iterator<Task> iterator(Feedback feedback) {
            return fbItBuilder.build(this, idSet.size(), feedback ).iterator();
        }

        @Override
        public Iterator<Task> iterator() {
            return new LazyIterator();
        }
    }
}
