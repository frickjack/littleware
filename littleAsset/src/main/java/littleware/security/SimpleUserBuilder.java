/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security;

import littleware.asset.SimpleAssetBuilder;
import littleware.base.validate.ValidationException;
import static littleware.security.LittleUser.Status;


/**
 * Simple implementation of the SimpleUserBuilder interface
 */
public class SimpleUserBuilder extends SimpleAssetBuilder implements LittleUser.Builder {

    /** Do-nothing constructor for java.io.Serializable */
    public SimpleUserBuilder() {
        super( SecurityAssetType.USER );
        setOwnerId( getId() );
    }

    @Override
    public Status getStatus() {
        if (getState() == Status.ACTIVE.ordinal()) {
            return Status.ACTIVE;
        }
        return Status.INACTIVE;
    }

    @Override
    public void setStatus(Status status) {
        setState(status.ordinal());
    }

    @Override
    public LittleUser.Builder status( Status status ) {
        setStatus( status );
        return this;
    }

    @Override
    public LittleUser.Builder state( int state ) {
        if ( (state < 0) || (state > Status.values().length - 1) ) {
            throw new IllegalArgumentException( "Illegal user state: " + state );
        }
        super.state( state );
        return this;
    }


    private static class User extends SimpleAssetBuilder.SimpleAsset implements LittleUser {

        /** For serialization */
        private User() {}

        public User( SimpleUserBuilder builder ) {
            super( builder );
        }
        
        @Override
        public Status getStatus() {
            final int state = getState();
            for ( Status status : Status.values() ) {
                if ( state == status.ordinal() ) {
                    return status;
                }
            }
            throw new ValidationException( "Unknown state: " + getState() );
        }

    }

    @Override
    public LittleUser build() {
        return new User( this );
    }

}

