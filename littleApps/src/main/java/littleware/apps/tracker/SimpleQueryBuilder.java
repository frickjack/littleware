/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import littleware.apps.tracker.TaskQuery.BuilderSetState;
import littleware.base.Maybe;

public class SimpleQueryBuilder implements TaskQuery.BuilderStart {

    public enum StatusMode {
        Active,
        Finished,
        All,
        InState;
    }
    
    public static class Query implements TaskQuery, Serializable {
        private UUID                    queueId;
        private StatusMode              statusMode = StatusMode.All;
        private Maybe<TaskStatus>       maybeStatus = Maybe.empty();
        private Maybe<UUID>             maybeAssignedTo = Maybe.empty();
        private Maybe<UUID>             maybeSubmittedBy = Maybe.empty();

        public Query() {}
        public Query( Queue queue ) {
            queueId = queue.getId();
        }
        public Query( Queue queue, StatusMode statusMode ) {
            this( queue );
            this.statusMode = statusMode;
        }
        public Query( Queue queue, TaskStatus status ) {
            this( queue, StatusMode.InState );
            this.maybeStatus = Maybe.something( status );
        }

        public UUID getQueueId() { return queueId; }
        public StatusMode getStatusMode() {
            return statusMode;
        }
    }
    
    @Override
    public BuilderSetState queue(Queue value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
