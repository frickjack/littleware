package littleware.security.auth.server.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.*;
//import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.security.auth.*;
import javax.security.auth.login.Configuration;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import littleware.asset.*;
import littleware.asset.AssetTreeTemplate.AssetInfo;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.asset.TemplateScanner.ExistInfo;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerScannerFactory;
import littleware.asset.server.ServerSearchManager;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.*;
import littleware.base.login.LoginCallbackHandler;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.security.auth.LittleSession.Builder;
import littleware.security.auth.internal.RemoteSessionManager;
import littleware.security.auth.server.ServerConfigFactory;

/**
 * Simple implementation of SessionManager.
 * Hands of authentication to new LoginContext,
 * then passes the authenticated Subject onto the SessionHelper.
 * This class ought to be registered as a Singleton and exported
 * for RMI access.
 */
public class SimpleSessionManager implements RemoteSessionManager {
    /**
     * Little helper class to simplify injection of some
     * runtime properties at construction time.
     * Currently just tracks a bootstrap user to auto-add to the admin group -
     * that admin can then handle adding other admins to the admin group or whatever.
     */
    public static class RuntimeConfig {
        public final Optional<String> optAdminUser;
        
        public RuntimeConfig( String adminUser ) {
            this.optAdminUser = Optional.ofNullable( adminUser );
        }
        
        public RuntimeConfig() {
            this.optAdminUser = Optional.empty();
        }
    }

    private static final Logger log = Logger.getLogger(SimpleSessionManager.class.getName());
    private static final long serialVersionUID = 8144056326046717141L;
    private static final String serverVersionName = "ServerVersion";
    private final ServerSearchManager search;
    private final ServerAssetManager assetMgr;
    private static boolean isSingletonUp = false;
    private final Provider<UserTreeBuilder> userTreeBuilder;
    private final Provider<GenericBuilder> provideGenerics;
    private final Provider<TemplateBuilder> templateProvider;
    private final Provider<AssetPathFactory> pathFactory;
    private final Provider<Builder> sessionProvider;
    private final LittleContext.ContextFactory contextFactory;
    private final ServerScannerFactory scannerFactory;
    private final Provider<Configuration> loginConfigProvider;
    private final RuntimeConfig runtimeConfig;

    
    @Inject
    public SimpleSessionManager(
            ServerAssetManager assetMgr,
            ServerSearchManager search,
            Provider<UserTreeBuilder> provideUserTree,
            Provider<GenericAsset.GenericBuilder> provideGenerics,
            Provider<AssetTreeTemplate.TemplateBuilder> templateProvider,
            Provider<AssetPathFactory> pathFactory,
            Provider<LittleSession.Builder> sessionProvider,
            LittleContext.ContextFactory contextFactory,
            ServerScannerFactory scannerFactory,
            ServerConfigFactory loginConfigProvider,
            RuntimeConfig     runtimeConfig
            ) throws RemoteException {
        this.assetMgr = assetMgr;
        this.search = search;
        if (isSingletonUp) {
            throw new IllegalStateException("SimpleSessionManager must be a singleton");
        }
        isSingletonUp = true;
        this.userTreeBuilder = provideUserTree;
        this.provideGenerics = provideGenerics;
        this.templateProvider = templateProvider;
        this.pathFactory = pathFactory;
        this.sessionProvider = sessionProvider;
        this.contextFactory = contextFactory;
        this.scannerFactory = scannerFactory;
        this.loginConfigProvider = loginConfigProvider;
        this.runtimeConfig = runtimeConfig;
    }

    /**
     * Save the given LittleSession to the appropriate location in the repository node graph
     */
    private LittleSession setupSession(LittleContext adminCtx, LittleSession session, String sessionComment) throws BaseException, GeneralSecurityException {
        final LittleHome home = search.getByName(adminCtx, "littleware.home", LittleHome.HOME_TYPE).get().narrow();

        // First - verify ServerVersion node exists -
        // TODO: find a better place to do this
        if ( ! search.getAssetFrom(adminCtx, home.getId(), serverVersionName).isPresent()) {
            // Only administrator can creat child of littleware.home ...
            assetMgr.saveAsset(adminCtx,
                    provideGenerics.get().parent(home).name(serverVersionName).data("v0.0").build(),
                    "Setup v0.0 ServerVersion node");
        }
        // Let's create a date-hierarchy to store the user session under
        final ZonedDateTime now = ZonedDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "/yyyy/MM/dd" );
        final AssetPath path = pathFactory.get().createPath("/" + home.getName() + 
                now.format( formatter ));
               
        final AssetTreeTemplate template = templateProvider.get().path(path).build();
        Asset parent = home;
        for (AssetInfo x : template.scan(home, scannerFactory.build(adminCtx))) {
            final TemplateScanner.ExistInfo info = (TemplateScanner.ExistInfo) x;
            parent = info.getAsset();
            if (!info.getAssetExists()) {
                // be sure to null out aclId, so random users cannot steal session ids
                parent = assetMgr.saveAsset(adminCtx,
                        info.getAsset().copy().aclId(null).build(),
                        sessionComment).get(info.getAsset().getId());
            }
        }

        return assetMgr.saveAsset(adminCtx,
                ((AbstractAssetBuilder) session.copy()).parentId(parent.getId()).homeId(parent.getHomeId()).build(),
                sessionComment).get(session.getId()).narrow();
    }

    /**
     * For now just authenticate anyone with a user account
     */
    @Override
    public LittleSession login(final String name, final String password, String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final LoginContext loginCtx;
        {
            LoginContext tmp = null;
            try {
                tmp = new LoginContext("littleware.login",
                        new Subject(),
                        new LoginCallbackHandler(name, password),
                        loginConfigProvider.get());
            } catch (Exception ex) {
                log.log(Level.INFO, "Assuming pass-through login - no littleware.login context available", ex);
            }
            loginCtx = tmp;
        }

        if (null != loginCtx) {
            try {
                loginCtx.login();
                final Subject caller = loginCtx.getSubject();
            } catch (FailedLoginException ex) {
                // dispose of cause - probably not serializable
                log.log(Level.WARNING, "Login failed", ex);
                throw new FailedLoginException();
            }
        }

        final LittleContext adminCtx = contextFactory.buildAdminContext();
        adminCtx.getTransaction().startDbAccess();
        try {
            final LittleHome littleHome = search.getByName(adminCtx, "littleware.home", LittleHome.HOME_TYPE).get().narrow(LittleHome.class);
            final LittleUser user;
            {
                final Optional<? extends Asset> maybeUser = search.getByName(adminCtx, name, LittleUser.USER_TYPE);
                if (!maybeUser.isPresent()) {
                    // Create the user
                    for (AssetTreeTemplate.AssetInfo assetInfo : userTreeBuilder.get().user(name).build().scan(littleHome, scannerFactory.build(adminCtx))) {
                        final ExistInfo treeInfo = (ExistInfo) assetInfo;
                        if (!treeInfo.getAssetExists()) {
                            assetMgr.saveAsset(adminCtx, treeInfo.getAsset(), "Setup new user: " + name);
                        }
                    }
                    user = search.getByName(adminCtx, name, LittleUser.USER_TYPE).get().narrow();
                } else {
                    user = maybeUser.get().narrow();
                }
            }
            
            //
            // auto-add the user to the admin group if configuration says this user is an admin
            // and not already in group.  Funny place to do this, but saves us from having
            // to hard-code a password for littleware.administrator someplace or whatever at
            // bootstrap time ...
            //
            if( runtimeConfig.optAdminUser.isPresent() && runtimeConfig.optAdminUser.get().equals( user.getName() ) ) {
                final LittleGroup adminGroup = search.getAsset(adminCtx, 
                      AccountManager.UUID_ADMIN_GROUP, -1
                    ).getAsset().get().narrow();
                
                if ( ! adminGroup.isMember(user) ) {
                    assetMgr.saveAsset(adminCtx, adminGroup.copy().add( user ).build(), "Add new user: " + user.getName() );
                }
            }
            
            // ok - user authenticated ok by here - setup user session
            final LittleSession session;
            {
                final LittleSession.Builder sessionBuilder = sessionProvider.get();
                sessionBuilder.setId(UUID.randomUUID());
                sessionBuilder.setName(name + "_" + UUIDFactory.makeCleanString(sessionBuilder.getId()));
                sessionBuilder.setOwnerId(user.getId());
                sessionBuilder.setComment("User login");
                session = sessionBuilder.build();

                if (session.getOwnerId() != user.getId()) {
                    throw new IllegalStateException("Owner mismatch");
                }
            }
            // Create the session asset as the admin user 
            return setupSession(adminCtx, session, sessionComment);
        } finally {
            adminCtx.getTransaction().endDbAccess();
        }
    }

    @Override
    public String getServerVersion() throws RemoteException {
        // Create the session asset as the admin user - session has null from-id
        final LittleContext adminCtx = contextFactory.buildAdminContext();
        try {
            final Asset home = search.getByName(adminCtx, "littleware.home", LittleHome.HOME_TYPE).get();
            final Optional<Asset> maybe = search.getAssetFrom(adminCtx, home.getId(), serverVersionName);

            if (maybe.isPresent()) {
                return maybe.get().narrow(GenericAsset.class).getData();
            } else {
                // Note: ServerVersionNode should be initialized in SessionManager if it doesn't exist
                return "v0.0";
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Unexpected exception: " + ex);
        } finally {
            adminCtx.getTransaction().endDbAccess();
        }
    }

    @Override
    public LittleSession createNewSession(UUID currentSessionId, String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final LittleContext adminCtx = contextFactory.buildAdminContext();
        adminCtx.getTransaction().startDbAccess();
        
        try {
            final LittleContext ctx = contextFactory.build(currentSessionId);
            final LittleSession.Builder sessionBuilder = sessionProvider.get();
            final LittleUser caller = ctx.getCaller();
            final UUID id = UUID.randomUUID();
            sessionBuilder.id( id
                    ).name(caller.getName() + "_" + UUIDFactory.makeCleanString( id )
                    ).comment( sessionComment
                    ).ownerId( caller.getId() );

            // Create the session asset as the admin user - session has null from-id
            return setupSession(adminCtx, sessionBuilder.build(), sessionComment);
        } finally {
            adminCtx.getTransaction().endDbAccess();
        }
    }
}
