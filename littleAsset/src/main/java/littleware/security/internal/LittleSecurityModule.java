/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security.internal;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittleGroupMember;
import littleware.security.LittleUser;
import littleware.security.Quota;
import littleware.security.auth.LittleSession;
import littleware.security.auth.internal.SimpleSessionBuilder;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Module binds and registers littleware.asset asset types
 */
public class LittleSecurityModule extends AbstractAppModule {
    public static class AppFactory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LittleSecurityModule(profile);
        }
    }

    //-------------------------------------

    public LittleSecurityModule( AppProfile profile ) {
        super( profile );
    }


    public static class Activator implements BundleActivator {

        @Inject
        public Activator( AssetProviderRegistry assetRegistry,
                 Provider<LittleUser.Builder> userProvider,
                 Provider<LittleGroup.Builder> groupProvider,
                 Provider<LittleGroupMember.MemberBuilder> memberProvider,
                 Provider<LittleAcl.Builder> aclProvider,
                 Provider<LittleAclEntry.Builder> aclEntryProvider,
                 Provider<Quota.Builder> quotaProvider,
                 Provider<LittleSession.Builder> sessionProvider
                ) {
            assetRegistry.registerService(LittleUser.USER_TYPE, userProvider );
            assetRegistry.registerService(LittleGroup.GROUP_TYPE, groupProvider );
            assetRegistry.registerService( LittleGroupMember.GROUP_MEMBER_TYPE, memberProvider );
            assetRegistry.registerService( LittleAcl.ACL_TYPE, aclProvider );
            assetRegistry.registerService( LittleAclEntry.ACL_ENTRY, aclEntryProvider );
            assetRegistry.registerService( Quota.QUOTA_TYPE, quotaProvider );
            assetRegistry.registerService( LittleSession.SESSION_TYPE, sessionProvider );
        }

        @Override
        public void start(BundleContext bc) throws Exception {
        }

        @Override
        public void stop(BundleContext bc) throws Exception {
        }

    }

    @Override
    public Class<? extends BundleActivator> getActivator() {
        return Activator.class;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind( Quota.Builder.class ).to( QuotaBuilder.class );
        binder.bind( LittleAcl.Builder.class ).to( SimpleACLBuilder.class );
        binder.bind( LittleAclEntry.Builder.class ).to( AclEntryBuilder.class );
        binder.bind( LittleGroup.Builder.class ).to( GroupBuilder.class );
        binder.bind( LittleGroupMember.MemberBuilder.class ).to( GroupMemberBuilder.class );
        binder.bind( LittleSession.Builder.class ).to( SimpleSessionBuilder.class );
        binder.bind( LittleUser.Builder.class ).to( SimpleUserBuilder.class );
    }

}
