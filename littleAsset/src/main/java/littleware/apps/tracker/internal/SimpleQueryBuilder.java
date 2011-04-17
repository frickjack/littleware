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

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.TaskQuery;
import littleware.apps.tracker.TaskQuery.BuilderSetState;
import littleware.apps.tracker.TaskStatus;
import littleware.base.Maybe;
import littleware.security.LittleUser;

/**
 * Internal TaskQuery implementation.
 * Clients should not access these classes directly -
 * implementation will change over time.
 */
public class SimpleQueryBuilder implements TaskQuery.BuilderStart {
    
    public static class Query implements TaskQuery, TaskQuery.BuilderSetState,
            TaskQuery.BuilderNarrow, TaskQuery.FinalBuilder, Serializable {
        private Maybe<Date> maybeMaxCreate = Maybe.empty();
        private Maybe<Date> maybeMinModify = Maybe.empty();
        private Maybe<Date> maybeMaxModify = Maybe.empty();
        private Maybe<String>  maybeTaskName = Maybe.empty();

        public enum StatusMode {
            Active,
            Finished,
            All,
            InState;
        }

        private UUID                    queueId;
        private StatusMode              statusMode = StatusMode.All;
        private Maybe<TaskStatus>       maybeStatus = Maybe.empty();
        private Maybe<UUID>             maybeAssignedTo = Maybe.empty();
        private Maybe<UUID>             maybeSubmittedBy = Maybe.empty();
        private Maybe<Date>             maybeMinCreate = Maybe.empty();

        @Override
        public FinalBuilder withTaskName( String value) {
            maybeTaskName = Maybe.something( value );
            return this;
        }

        public Maybe<String> getTaskName() {
            return maybeTaskName;
        }

        @Override
        public BuilderNarrow anyStatus() {
            statusMode = StatusMode.All;
            return this;
        }

        @Override
        public BuilderNarrow minCreateDate(Date value) {
            maybeMinCreate = Maybe.something( value );
            return this;
        }
        public Maybe<Date> getMinCreateDate() { return maybeMinCreate; }

        @Override
        public BuilderNarrow maxCreateDate(Date value) {
            maybeMaxCreate = Maybe.something( value );
            return this;
        }
        public Maybe<Date> getMaxCreateDate() { return maybeMaxCreate; }

        @Override
        public BuilderNarrow minModifyDate(Date value) {
            maybeMinModify = Maybe.something( value );
            return this;
        }
        public Maybe<Date> getMinModifyDate() { return maybeMinModify; }

        @Override
        public BuilderNarrow maxModifyDate(Date value) {
            maybeMaxModify = Maybe.something( value );
            return this;
        }
        public Maybe<Date> getMaxModifyDate() { return maybeMaxModify; }
        
        public Query() {}
        public Query( Queue queue ) {
            queueId = queue.getId();
        }

        @Override
        public BuilderNarrow active() {
            statusMode = StatusMode.Active;
            return this;
        }

        @Override
        public BuilderNarrow finished() {
            statusMode = StatusMode.Finished;
            return this;
        }

        @Override
        public BuilderNarrow inState(TaskStatus value) {
            statusMode = statusMode.InState;
            maybeStatus = Maybe.something( value );
            return this;
        }

        @Override
        public TaskQuery build() {
            return this;
        }

        @Override
        public BuilderNarrow assignedTo(LittleUser value) {
            this.maybeAssignedTo = Maybe.something( value.getId() );
            return this;
        }

        @Override
        public BuilderNarrow submittedBy(LittleUser value) {
            this.maybeSubmittedBy = Maybe.something(value.getId() );
            return this;
        }

        public UUID getQueueId() { return queueId; }
        public StatusMode getStatusMode() {
            return statusMode;
        }
        public Maybe<UUID> getAssignedTo() { return maybeAssignedTo; }
        public Maybe<UUID> getSubmittedBy() { return maybeSubmittedBy; }
        public Maybe<TaskStatus> getStatus() { return maybeStatus; }
    }
    
    @Override
    public BuilderSetState queue(Queue value) {
        return new Query( value );
    }
}
