package littleware.security.internal;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.util.Arrays;
import java.util.Optional;
import littleware.asset.gson.GsonAssetAdapter;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.security.Everybody;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittleGroupMember;
import littleware.security.LittleUser;
import littleware.security.Quota;
import littleware.security.auth.LittleSession;
import littleware.security.auth.internal.SimpleSessionBuilder;



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


    public static class Activator implements LifecycleCallback {

        @Inject
        public Activator( AssetProviderRegistry assetRegistry,
                 Provider<LittleUser.Builder> userProvider,
                 Provider<LittleGroup.Builder> groupProvider,
                 Provider<LittleGroupMember.MemberBuilder> memberProvider,
                 Provider<LittleAcl.Builder> aclProvider,
                 Provider<LittleAclEntry.Builder> aclEntryProvider,
                 Provider<Quota.Builder> quotaProvider,
                 Provider<LittleSession.Builder> sessionProvider,
                 LittleGsonFactory gsonFactory,
                 GroupGsonAdapter gsonGroup,
                 AclGsonAdapter  gsonAcl,
                 UserGsonAdapter gsonUser
                ) {
            assetRegistry.registerService(LittleUser.USER_TYPE, userProvider );
            assetRegistry.registerService(LittleGroup.GROUP_TYPE, groupProvider );
            assetRegistry.registerService( LittleGroupMember.GROUP_MEMBER_TYPE, memberProvider );
            assetRegistry.registerService( LittleAcl.ACL_TYPE, aclProvider );
            assetRegistry.registerService( LittleAclEntry.ACL_ENTRY, aclEntryProvider );
            assetRegistry.registerService( Quota.QUOTA_TYPE, quotaProvider );
            assetRegistry.registerService( LittleSession.SESSION_TYPE, sessionProvider );
            for( GsonAssetAdapter adapter : Arrays.asList( gsonGroup, gsonAcl, gsonUser )) {
                gsonFactory.registerAssetAdapter(adapter);
            }
        }

        @Override
        public void startUp(){}

        @Override
        public void shutDown(){}

    }

    @Override
    public Optional<Class<Activator>> getCallback() {
        return Optional.of( Activator.class );
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
        binder.bind( Everybody.class ).toInstance( SimpleEverybody.singleton );
    }

}
