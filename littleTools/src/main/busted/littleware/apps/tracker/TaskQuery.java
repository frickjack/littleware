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

import littleware.apps.tracker.internal.SimpleQueryBuilder;
import com.google.inject.ImplementedBy;
import java.util.Date;
import littleware.security.LittleUser;

/**
 * Opaque query definition for TaskManager loadTasksSet( TaskQuery query )
 * with builder that implements domain-specific language.
 */
public interface TaskQuery {

    public interface FinalBuilder {
        TaskQuery  build();
    }
    
    @ImplementedBy(SimpleQueryBuilder.class)
    public interface BuilderStart {
        public BuilderSetState queue( Queue value );
    }

    public interface BuilderSetState extends FinalBuilder {
        public BuilderNarrow  anyStatus();
        public BuilderNarrow active();
        public BuilderNarrow finished();
        public BuilderNarrow inState( TaskStatus value );
        public FinalBuilder   withTaskName( String value );
    }

    public interface BuilderNarrow extends FinalBuilder {
        public BuilderNarrow assignedTo( LittleUser value );
        public BuilderNarrow submittedBy( LittleUser value );
        /** Supports new-task reports */
        public BuilderNarrow minCreateDate( Date value );
        public BuilderNarrow maxCreateDate( Date value );
        /** Supports RSS feed */
        public BuilderNarrow minModifyDate( Date value );
        public BuilderNarrow maxModifyDate( Date value );
    }
}
