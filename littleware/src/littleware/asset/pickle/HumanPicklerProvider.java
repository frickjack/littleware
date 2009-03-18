/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.pickle;

import com.google.inject.Provider;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
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
public final class HumanPicklerProvider implements Provider<AssetHumanPickler> {

    private final Map<AssetType<? extends Asset>,Provider<? extends AssetHumanPickler>> omapType2Provider =
            new HashMap<AssetType<? extends Asset>,Provider<? extends AssetHumanPickler>>();

    private final Provider<? extends AssetHumanPickler> oprovideDefault;

    /**
     * Inject a default pickler factory that handles every asset
     * type that does not register a custom pickler.
     *
     * @param provideDefault
     */
    public HumanPicklerProvider( Provider<? extends AssetHumanPickler> provideDefault ) {
        oprovideDefault = provideDefault;
        if ( oprovideDefault instanceof HumanPicklerProvider ) {
            throw new IllegalArgumentException( "Invalid default provider has class: " + 
                    oprovideDefault.getClass() );
        }
    }
    
    /**
     * Just set default pickle provider to supply SimpleHumanPickler
     */
    public HumanPicklerProvider() {
        oprovideDefault = new Provider<SimpleHumanPickler>() {

            @Override
            public SimpleHumanPickler get() {
                return new SimpleHumanPickler();
            }
            
        };
    }
    /**
     * Plugin a special pickler for a speicific asset type.
     * Will also handle subtypes of atype that don't
     * have their own specializer registered.
     *
     * @param atype to provide a custom pickler for
     * @param provideSpecial pickler factory
     */
    public void registerSpecializer( AssetType<?> atype,
            Provider<? extends AssetHumanPickler> provideSpecial
            )
    {
        omapType2Provider.put(atype, provideSpecial );
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
            for ( AssetType<? extends Asset> atype = x.getAssetType();
                  (null == provider) && (atype != null);
                  atype = atype.getSuperType()
                  ) {
                provider = omapType2Provider.get( atype );
            }
            if ( null == provider ) {
                provider = oprovideDefault;
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
