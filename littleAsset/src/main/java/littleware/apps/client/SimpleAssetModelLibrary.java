/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.client;

import com.google.inject.Inject;
import littleware.base.feedback.SimpleLittleTool;
import littleware.base.feedback.LittleListener;
import littleware.base.feedback.LittleEvent;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.event.AssetModelEvent;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.Whatever;
import littleware.base.cache.Cache;

/**
 * Simple implementation of AssetModelLibrary interface for
 * in-memory asset model cache.
 * Intended to be a singleton PER-USER.
 */
@Singleton
public class SimpleAssetModelLibrary 
        implements AssetModelLibrary {

    private static final Logger log = Logger.getLogger(SimpleAssetModelLibrary.class.getName());
    private final Cache<UUID,AssetModel> cache;

    @Override
    public Maybe<Asset> retrieveAsset(UUID u_id, AssetRetriever retriever) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Maybe<AssetModel> maybe = retrieveAssetModel(u_id, retriever);
        if (!maybe.isSet()) {
            return Maybe.empty();
        }
        return Maybe.something(maybe.get().getAsset());
    }

    @Override
    public Policy getPolicy() {
        return cache.getPolicy();
    }

    @Override
    public int getMaxSize() {
        return cache.getMaxSize();
    }

    @Override
    public int getMaxEntryAgeSecs() {
        return cache.getMaxEntryAgeSecs();
    }

    @Override
    public AssetModel put(UUID k, AssetModel v) {
        return cache.put( k, v );
    }

    @Override
    public AssetModel get(UUID k) {
        return cache.get( k );
    }

    @Override
    public void clear() {
        cache.clear();
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public boolean isEmpty() {
        return cache.isEmpty();
    }

    @Override
    public Map<UUID, AssetModel> cacheContents() {
        return cache.cacheContents();
    }

    /**
     * Simple implementation of AssetModel interface
     */
    private class SimpleAssetModel implements AssetModel {

        private Asset oa_data;
        private SimpleLittleTool otool_support = new SimpleLittleTool(this);

        /**
         * Constructor associates an asset 
         */
        public SimpleAssetModel(Asset a_data) {
            oa_data = a_data;
        }

        @Override
        public Asset getAsset() {
            return oa_data;
        }

        /**
         * Compute the LittleEvents this thing should throw
         * assuming we sync a_old with a_new.
         */
         public List<AssetModelEvent> computeEvents(Asset a_old, Asset a_new) {
            final List<AssetModelEvent> vResult = new ArrayList<AssetModelEvent>();
            if ((a_new == null) || (a_new == a_old)) {
                return vResult;
            }

            // update by-name dictionary
            if (a_new.getAssetType().isNameUnique() 
                    && ((null == a_old)
                        || (!a_old.getName().equals(a_new.getName()))
                        )
                        ) {
                if (null != a_old) {
                    for (AssetType atype = a_old.getAssetType();
                            (atype != null) && atype.isNameUnique();
                            atype = (AssetType) atype.getSuperType().getOr(null)) {
                        omulti_byname.get(atype).remove(a_old.getName());
                    }
                }
                for (AssetType atype = a_new.getAssetType();
                        (null != atype) && atype.isNameUnique();
                        atype = (AssetType) atype.getSuperType().getOr(null)) {
                    Map<String, UUID> map_4type = omulti_byname.get(atype);
                    if (null == map_4type) {
                        map_4type = new HashMap<String, UUID>();
                        omulti_byname.put(atype, map_4type);
                    }
                    map_4type.put(a_new.getName(), a_new.getId());
                }
            }

            final AssetModelEvent event = new AssetModelEvent(this, AssetModel.Operation.assetUpdated);
            vResult.add(event);

            // a_old is null at initial register time
            final UUID uFromOld = (null == a_old) ? null : a_old.getFromId();
            if (!Whatever.get().equalsSafe(uFromOld, a_new.getFromId() )) {
                for (UUID uFrom : Arrays.asList(uFromOld, a_new.getFromId() )) {
                    if ( null == uFrom ) {
                        continue;
                    }
                    final SimpleAssetModel amodel_affected = (SimpleAssetModel) get(uFrom);
                    if (null == amodel_affected) {
                        continue;
                    }
                    vResult.add( new AssetModelEvent(amodel_affected,
                            AssetModel.Operation.assetsLinkingFrom,
                            null,
                            event
                            ) );
                }
            }
            // a_old is null at initial register time
            final UUID uToOld = (null == a_old) ? null : a_old.getToId();
            if (!Whatever.get().equalsSafe(uToOld, a_new.getToId() )) {
                for (UUID uTo : Arrays.asList(uToOld, a_new.getToId() )) {
                    if ( null == uTo ) {
                        continue;
                    }
                    final SimpleAssetModel amodel_affected = (SimpleAssetModel) get(uTo);
                    if (null == amodel_affected) {
                        continue;
                    }
                    vResult.add( new AssetModelEvent(amodel_affected,
                            AssetModel.Operation.assetsLinkingTo,
                            null,
                            event
                            ) );
                }
            }

            return vResult;
        }

        /**
         * Call out to SimpleAssetModelLibrary.sycnAsset to
         * fire AssetModelEvent on other affected asset-models.
         */
        @Override
        public Asset syncAsset(Asset a_new) {
            log.log(Level.FINE, "Syncing: " + a_new);

            if ((null == a_new) || (oa_data == a_new)) {
                return oa_data;
            }
            if ((null != oa_data)
                    && (a_new.getTimestamp() <= oa_data.getTimestamp()) ) {
                return oa_data;
            }
            final UUID fromIdOld = (null != oa_data) ? oa_data.getFromId() : null;
            final UUID fromIdNew = a_new.getFromId();
            // this call syncs a_new with oa_data
            final List<AssetModelEvent> vEvent = computeEvents( oa_data, a_new );
            oa_data = a_new;
            for( AssetModelEvent event : vEvent ) {
                ((SimpleAssetModel) event.getSource()).fireLittleEvent( event );
            }
            return this.getAsset();
        }

        @Override
        public AssetModelLibrary getLibrary() {
            return SimpleAssetModelLibrary.this;
        }

        /**
         * Just return the hashCode for the wrapped asset
         */
        @Override
        public int hashCode() {
            return oa_data.hashCode();
        }

        @Override
        public String toString() {
            return "AssetModel(" + oa_data.toString() + ")";
        }

        /**
         * Just compare the wrapped assets
         */
        @Override
        public int compareTo(AssetModel model_other) {
            return oa_data.compareTo(model_other.getAsset());
        }

        /** Just compare the wrapped assets */
        @Override
        public boolean equals(Object x_other) {
            return ((null != x_other) && (x_other instanceof SimpleAssetModel) && oa_data.equals(((SimpleAssetModel) x_other).oa_data));
        }

        /**
         * Internal convenience - calls through to internal SimpleLittleTool
         */
        void fireLittleEvent(LittleEvent event_fire) {
            otool_support.fireLittleEvent(event_fire);
        }

        @Override
        public void addLittleListener(LittleListener listen_add) {
            otool_support.addLittleListener(listen_add);
        }

        @Override
        public void removeLittleListener(LittleListener listen_remove) {
            otool_support.removeLittleListener(listen_remove);
        }
    }
    private Map<AssetType, Map<String, UUID>> omulti_byname =
            new TreeMap<AssetType, Map<String, UUID>>();

    @Override
    public synchronized Maybe<AssetModel> getByName(String s_name, AssetType atype) throws InvalidAssetTypeException {
        if (!atype.isNameUnique()) {
            throw new InvalidAssetTypeException("Asset type not name-unique: " + atype);
        }
        final Map<String, UUID> map_byname = omulti_byname.get(atype);
        if (null == map_byname) {
            return Maybe.empty();
        }
        UUID u_id = map_byname.get(s_name);
        if (null == u_id) {
            return Maybe.empty();
        }
        return Maybe.emptyIfNull(get(u_id));
    }

    @Override
    public Maybe<AssetModel> getByName(String s_name, AssetType atype,
            AssetSearchManager m_search) throws InvalidAssetTypeException,
            BaseException,
            AssetException, GeneralSecurityException, RemoteException {
        Maybe<AssetModel> maybeModel = getByName(s_name, atype);

        if (maybeModel.isSet()) {
            return maybeModel;
        }
        final Maybe<? extends Asset> maybeAsset = m_search.getByName(s_name, atype);
        if (!maybeAsset.isSet()) {
            return Maybe.empty();
        }
        return Maybe.something(syncAsset(maybeAsset.get()));
    }

    /**
     * Constructor initializes the underlying SimpleCache with huge
     * timeout and max-size
     */
    @Inject
    public SimpleAssetModelLibrary( Cache.Builder cacheBuilder ) {
        cache = cacheBuilder.maxAgeSecs(10000000 ).maxSize( 1000000).build();
    }

    @Override
    public synchronized AssetModel syncAsset(Asset a_new) {
        if (null == a_new) {
            return null;
        }
        AssetModel amodel_lookup = this.get(a_new.getId());

        if (null == amodel_lookup) {
            amodel_lookup = new SimpleAssetModel(a_new);
            this.put(a_new.getId(), amodel_lookup);
            final List<AssetModelEvent> vEvent = ((SimpleAssetModel) amodel_lookup).computeEvents( null, a_new );
            for( AssetModelEvent event : vEvent ) {
                ((SimpleAssetModel) event.getSource()).fireLittleEvent( event );
            }
        } else {
            amodel_lookup.syncAsset(a_new);
        }

        return amodel_lookup;
    }

    @Override
    public synchronized Maybe<AssetModel> retrieveAssetModel(UUID u_id, AssetRetriever m_retriever) throws BaseException,
            AssetException, GeneralSecurityException, RemoteException {
        Maybe<AssetModel> maybeModel = Maybe.emptyIfNull(get(u_id));

        if (maybeModel.isSet()) {
            return maybeModel;
        }
        Maybe<Asset> maybeAsset = m_retriever.getAsset(u_id);
        if (!maybeAsset.isSet()) {
            return Maybe.empty();
        }
        return Maybe.something(syncAsset(maybeAsset.get()));
    }

    @Override
    public Collection<AssetModel> syncAsset(Collection<? extends Asset> v_assets) {
        List<AssetModel> v_result = new ArrayList<AssetModel>();
        for (Asset a_check : v_assets) {
            v_result.add(syncAsset(a_check));
        }
        return v_result;
    }

    @Override
    public AssetModel remove(UUID u_remove) {
        AssetModel amodel_remove = cache.remove(u_remove);
        if ((null != amodel_remove) && amodel_remove.getAsset().getAssetType().isNameUnique()) {
            Asset a_remove = amodel_remove.getAsset();
            for (AssetType atype = a_remove.getAssetType();
                    (atype != null) && atype.isNameUnique();
                    atype = (AssetType) atype.getSuperType().getOr(null)) {
                Map<String, UUID> map_byname = omulti_byname.get(atype);
                if (null != map_byname) {
                    map_byname.remove(a_remove.getName());
                }
            }
        }
        return amodel_remove;
    }

    @Override
    public Maybe<AssetModel> assetDeleted(UUID u_deleted) {
        final SimpleAssetModel amodel_deleted = (SimpleAssetModel) remove(u_deleted);

        if (null != amodel_deleted) {
            final Asset aDelete = amodel_deleted.getAsset();
            AssetModelEvent event_delete = new AssetModelEvent(amodel_deleted,
                    AssetModel.Operation.assetDeleted);
            amodel_deleted.fireLittleEvent(event_delete);
            if ( null != aDelete.getFromId() ) {
                final SimpleAssetModel parent = (SimpleAssetModel) get( aDelete.getFromId() );
                if ( null != parent ) {
                    parent.fireLittleEvent(
                            new AssetModelEvent( parent,
                            AssetModel.Operation.assetsLinkingFrom,
                            null,
                            event_delete
                            ) );
                }
            }
        }
        return Maybe.emptyIfNull((AssetModel) amodel_deleted);
    }
}

