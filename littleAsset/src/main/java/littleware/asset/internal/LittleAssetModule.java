package littleware.asset.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import littleware.asset.client.internal.InMemorySearchMgrProxy;
import littleware.asset.client.internal.InMemoryAssetMgrProxy;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import java.util.Optional;
import java.util.Properties;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.GenericAsset;
import littleware.asset.IdWithClock;
import littleware.asset.LinkAsset;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.gson.GsonAssetAdapter;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.gson.internal.GenericAdapter;
import littleware.asset.gson.internal.GsonProvider;
import littleware.asset.gson.internal.HomeAdapter;
import littleware.asset.gson.internal.LinkAdapter;
import littleware.asset.gson.internal.TreeNodeAdapter;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.asset.pickle.XmlPicklerProvider;
import littleware.asset.pickle.internal.SimpleHumanRegistry;
import littleware.asset.pickle.internal.SimpleXmlRegistry;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.asset.spi.internal.SimpleAssetRegistry;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.bootstrap.helper.AbstractAppModule;
import littleware.security.auth.client.internal.InMemorySessionMgrProxy;
import littleware.security.auth.client.internal.RemoteSessionMgrProxy;

import org.apache.http.HttpHost;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;


/**
 * Module binds and registers littleware.asset asset types
 */
public class LittleAssetModule extends AbstractAppModule {

    public static class AppFactory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new LittleAssetModule(profile);
        }
    }

    //---------------------------------------
    /**
     * Client-side configuration
     */
    public static class ClientConfig {

        /**
         * Remoting method - default is InMemory if an in-process server is
         * running, otherwise RMI. REST is not yet fully implemented.
         */
        public enum RemoteMethod {
            InMemory, REST
        }
        private RemoteMethod mode = RemoteMethod.InMemory;

        public RemoteMethod getRemoteMethod() {
            return mode;
        }

        public void setRemoteMethod(RemoteMethod value) {
            mode = value;
            if (value.equals(RemoteMethod.REST)) {
                throw new UnsupportedOperationException("REST mode not yet available");
            }
        }
    }
    
    
    private static final ClientConfig clientConfig = new ClientConfig();

    public static ClientConfig getClientConfig() {
        return clientConfig;
    }

    //-------------------------------------
    public LittleAssetModule(AppProfile profile) {
        super(profile);
    }

    public static class Activator implements LifecycleCallback {

        @Inject
        public Activator(AssetProviderRegistry assetRegistry,
                Provider<TreeNode.TreeNodeBuilder> nodeProvider,
                Provider<GenericAsset.GenericBuilder> genericProvider,
                Provider<LinkAsset.LinkBuilder> linkProvider,
                Provider<LittleHome.HomeBuilder> homeProvider,
                LittleGsonFactory gsonFactory,
                HomeAdapter gsonHomeAdapter,
                LinkAdapter gsonLinkAdapter,
                TreeNodeAdapter gsonTreeNodeAdapter,
                GenericAdapter gsonGenericAdapter) {
            assetRegistry.registerService(LittleHome.HOME_TYPE, homeProvider);
            assetRegistry.registerService(TreeNode.TREE_NODE_TYPE, nodeProvider);
            assetRegistry.registerService(GenericAsset.GENERIC, genericProvider);
            assetRegistry.registerService(LinkAsset.LINK_TYPE, linkProvider);
            for (GsonAssetAdapter adapter : new GsonAssetAdapter[]{
                        gsonHomeAdapter, gsonLinkAdapter, gsonTreeNodeAdapter, gsonGenericAdapter
                    }) {
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

    public static class GsonBuilderFactory implements Provider<GsonBuilder> {
        private final LittleGsonFactory factory;
        @Inject
        public GsonBuilderFactory( LittleGsonFactory factory ) {
            this.factory = factory;
        }
        
        @Override
        public GsonBuilder get() {
            return factory.getBuilder();
        }
    }
    
    @Override
    public void configure(Binder binder) {
        binder.bind(LittleHome.HomeBuilder.class).to(LittleHomeBuilder.class);
        binder.bind(GenericAsset.GenericBuilder.class).to(SimpleGenericBuilder.class);
        binder.bind(TreeNode.TreeNodeBuilder.class).to(SimpleTreeNodeBuilder.class);
        binder.bind(LinkAsset.LinkBuilder.class).to(SimpleLinkBuilder.class);
        binder.bind(IdWithClock.Builder.class).to(IdWithClockBuilder.class);
        binder.bind(AssetPathFactory.class).to(SimpleAssetPathFactory.class);
        binder.bind(AssetTreeTemplate.TemplateBuilder.class).to(SimpleTemplateBuilder.class).in(Scopes.SINGLETON);
        binder.bind(HumanPicklerProvider.class).to(SimpleHumanRegistry.class).in(Scopes.SINGLETON);
        binder.bind(XmlPicklerProvider.class).to(SimpleXmlRegistry.class).in(Scopes.SINGLETON);
        binder.bind(AssetProviderRegistry.class).to(SimpleAssetRegistry.class).in(Scopes.SINGLETON);
        binder.bind(LittleGsonFactory.class).to(GsonProvider.class).in(Scopes.SINGLETON);
        binder.bind( Gson.class ).toProvider( LittleGsonFactory.class );
        binder.bind( GsonBuilder.class ).toProvider( GsonBuilderFactory.class );
        binder.bind( GsonBuilderFactory.class ).in( Scopes.SINGLETON );
        
        try {  // Make sure all needed properties are set
            final Properties props = littleware.base.PropertiesLoader.get().loadProperties();
            if( null == props.getProperty( "littleware.rmi_host", null ) ) {
                binder.bindConstant().annotatedWith( Names.named( "littleware.rmi_host" ) ).to( "localhost" );
            }
            if( null == props.getProperty( "int.lw.rmi_port", null ) ) {
                binder.bindConstant().annotatedWith( Names.named( "int.lw.rmi_port" ) ).to( "1239" );
            }
            if( null == props.getProperty( "littleware.jndi.prefix", null ) ) {
                binder.bindConstant().annotatedWith( Names.named( "littleware.jndi.prefix" ) ).to( "//localhost:1239/" );
            }            
        } catch ( java.io.IOException ex ) {
            throw new IllegalStateException( "Failed accessing littleware.properties", ex );
        }

        final PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        // Increase max total connection to 200
        cm.setMaxTotal(200);
        // Increase default max connection per route to 20
        cm.setDefaultMaxPerRoute(20);
        // Increase max connections for localhost:80 to 50
        HttpHost localhost = new HttpHost("locahost", 80);
        cm.setMaxPerRoute(new HttpRoute(localhost), 50);
        final CredentialsProvider credProvider = new BasicCredentialsProvider();
        CloseableHttpClient httpClient = HttpClients.custom()
        .setConnectionManager(cm)
        .setDefaultCredentialsProvider(credProvider)
        .build();

        //httpclient.getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        //httpclient.getParams().setParameter(ClientPNames.HANDLE_REDIRECTS, java.lang.Boolean.FALSE);
        //httpclient.getParams().setParameter(org.apache.http.client.params.ClientPNames.ALLOW_CIRCULAR_REDIRECTS, java.lang.Boolean.TRUE);

        binder.bind(HttpClientConnectionManager.class).toInstance(cm);
        //binder.bind(DefaultHttpClient.class).toInstance(defaultClient);
        binder.bind(CredentialsProvider.class).toInstance(credProvider);
        binder.bind(HttpClient.class).toInstance(httpClient);              
                
        // Bind client method of connecting with server
        switch (getClientConfig().getRemoteMethod()) {
            case InMemory: {
                /* ... no client-side binding necessary ...
                binder.bind(RemoteAssetManager.class).to(InMemoryAssetMgrProxy.class).in(Scopes.SINGLETON);
                binder.bind(RemoteSearchManager.class).to(InMemorySearchMgrProxy.class).in(Scopes.SINGLETON);
                binder.bind(RemoteSessionMgrProxy.class).to(InMemorySessionMgrProxy.class).in(Scopes.SINGLETON);
                */
            }
              break;
            case REST: {
                throw new UnsupportedOperationException("REST client currently busted");
            } 
        }
    }
}
