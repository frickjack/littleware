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

import com.google.inject.ImplementedBy;
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
        public BuilderSetUser active();
        public BuilderSetUser finished();
        public BuilderSetUser inState( TaskStatus value );
    }

    public interface BuilderSetUser extends FinalBuilder {
        public FinalBuilder assignedTo( LittleUser value );
        public FinalBuilder submittedBy( LittleUser value );
    }
}