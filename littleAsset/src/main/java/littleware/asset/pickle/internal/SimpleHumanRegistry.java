package littleware.asset.pickle.internal;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.asset.pickle.AssetHumanPickler;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.BaseException;

/**
 * Factory for PickleHuman that supports AssetType based
 * specialization of serialization - tries to implement a flywheel pattern.
 * OSGi activators should register custom picklers at startup time.
 * Concurrent calls to get() are thread safe, but registerSpecializer
 * is not safe.
 *
 * @TODO clean this up, generalize for different picklers, and improve type-lookup performance
 */
@Singleton
public class SimpleHumanRegistry implements HumanPicklerProvider {

    private final Map<AssetType,Provider<? extends AssetHumanPickler>> type2ProviderMap =
            new HashMap<>();

    private final Provider<? extends AssetHumanPickler> defaultProvider;

    /**
     * Inject a default pickler factory that handles every asset
     * type that does not register a custom pickler.
     *
     * @param provideDefault
     */
    public SimpleHumanRegistry( Provider<? extends AssetHumanPickler> provideDefault ) {
        defaultProvider = provideDefault;
        if ( defaultProvider instanceof HumanPicklerProvider ) {
            throw new IllegalArgumentException( "Invalid default provider has class: " +
                    defaultProvider.getClass() );
        }
    }

    /**
     * Just set default pickle provider to supply SimpleHumanPickler
     */
    public SimpleHumanRegistry() {
        defaultProvider = () -> new SimpleHumanPickler();
    }
    /**
     * Plugin a special pickler for a speicific asset type.
     * Will also handle subtypes of atype that don't
     * have their own specializer registered.
     *
     * @param atype to provide a custom pickler for
     * @param provideSpecial pickler factory
     */
    @Override
    public void registerSpecializer( AssetType atype,
            Provider<? extends AssetHumanPickler> provideSpecial
            )
    {
        type2ProviderMap.put(atype, provideSpecial );
    }

    /**
     * Internal pickler with brains to look for type-specific pickle handlers
     */
    private class MyPickle implements AssetHumanPickler {

        @Override
        public Asset unpickle(Reader reader) throws AssetException, BaseException, GeneralSecurityException, IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void pickle(Asset x, Writer writer) throws AssetException, BaseException, GeneralSecurityException, IOException {
            Provider<? extends AssetHumanPickler> provider = null;
            for ( AssetType atype = x.getAssetType();
                  (null == provider) && (atype != null);
                  atype = atype.getSuperType().orElse(null)
                  ) {
                provider = type2ProviderMap.get( atype );
            }
            if ( null == provider ) {
                provider = defaultProvider;
            }
            provider.get().pickle(x, writer);
        }
    }

    @Override
    public AssetHumanPickler get()
    {
        return new MyPickle();
    }

}
