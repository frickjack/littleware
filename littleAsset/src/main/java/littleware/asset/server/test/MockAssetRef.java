/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server.test;

import java.beans.PropertyChangeListener;
import java.util.UUID;
import java.util.concurrent.Callable;
import littleware.asset.Asset;
import littleware.asset.client.AssetRef;
import littleware.base.Option;
import littleware.base.event.LittleListener;

/**
 *
 * @author pasquini
 */
public class MockAssetRef implements AssetRef {
    private final Option<Asset> ref;

    public MockAssetRef( Option<Asset> ref ) {
        this.ref = ref;
    }

    @Override
    public AssetRef updateRef(Asset value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Asset syncAsset(Asset asset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public UUID getId() {
        return ref.get().getId();
    }

    @Override
    public long getTimestamp() {
        return ref.get().getTimestamp();
    }

    @Override
    public boolean isSet() {
        return ref.isSet();
    }

    @Override
    public boolean isEmpty() {
        return ref.isEmpty();
    }

    @Override
    public Asset getOr(Asset t) {
        return ref.getOr(t);
    }

    @Override
    public Asset getOrCall(Callable<Asset> clbl) throws Exception {
        return ref.getOrCall(clbl);
    }

    @Override
    public Asset get() {
        return ref.get();
    }

    @Override
    public Asset getRef() {
        return get();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addLittleListener(LittleListener ll) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeLittleListener(LittleListener ll) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
