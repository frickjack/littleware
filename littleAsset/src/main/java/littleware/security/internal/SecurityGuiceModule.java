/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.internal;

import com.google.inject.Binder;
import com.google.inject.Module;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittleGroupMember;
import littleware.security.Quota;
import littleware.security.auth.LittleSession;
import littleware.security.auth.internal.SimpleSessionBuilder;

/**
 * Guice bindings common to both client and server bootstrap setups
 */
public class SecurityGuiceModule implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind( Quota.Builder.class ).to( QuotaBuilder.class );
        binder.bind( LittleAcl.Builder.class ).to( SimpleACLBuilder.class );
        binder.bind( LittleAclEntry.Builder.class ).to( AclEntryBuilder.class );
        binder.bind( LittleGroup.Builder.class ).to( GroupBuilder.class );
        binder.bind( LittleGroupMember.MemberBuilder.class ).to( GroupMemberBuilder.class );
        binder.bind( LittleSession.Builder.class ).to( SimpleSessionBuilder.class );
    }

}
