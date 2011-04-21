/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.pickle.internal;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import littleware.asset.AssetType;
import littleware.asset.pickle.AssetXmlPickler;
import littleware.asset.pickle.XmlPicklerProvider;

@Singleton
public class SimpleXmlRegistry implements XmlPicklerProvider {

    @Override
    public void registerSpecializer(AssetType assetType, Provider<? extends AssetXmlPickler> provideSpecial) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AssetXmlPickler get() {
        return new PickleXml();
    }

}
