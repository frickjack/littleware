/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.filebucket.server.DeleteCBProvider;
import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.*;
import littleware.db.*;
import littleware.security.*;

/**
 * Simple implementation of AssetManager interface.
 * Accesses a single database data source by which the manager
 * may add/update/search assets.
 * Delegates id-based getAsset asset-retrieval to
 * a suplied AssetRetriever - or access the shared resources
 * multi-source insecure and non-cacheing AssetRetriever
 * from AssetSharedResources.
 * SimpleAssetManager maintains (gets/puts/flushes) its own cache
 * supplied to the constructor
 * which also takes care of propagating cache-flush calls
 * to cache-cluster members.
 * Enforces READ/WRITE security by checking asset ownership and ACL -
 * requires support of AccountManager and AclManager implementations.
 * Has a recursive dependency on AccountManager and AclManager -
 * if set*Manager is not invoked, then method invocation will
 * attempt to initialize those managers from the security ResourceBundle.
 */
public class SimpleAssetManager implements AssetManager {

    private static final Logger log = Logger.getLogger(SimpleAssetManager.class.getName());
    private final DbAssetManager dbMgr;
    private final CacheManager cache;
    private final AssetSearchManager search;
    private final Factory<UUID> uuidFactory = UUIDFactory.getFactory();
    private final QuotaUtil quotaUtil;
    private final AssetSpecializerRegistry specialRegistry;
    private final Provider<LittleTransaction> provideTrans;
    private final Provider<LittleTransaction> provideSaveCycle =
            new ThreadLocalProvider<LittleTransaction>() {

                @Override
                protected LittleTransaction build() {
                    return new AbstractLittleTransaction() {

                        @Override
                        protected void endDbAccess(int iLevel) {
                        }

                        @Override
                        protected void endDbUpdate(boolean b_rollback, int iUpdateLevel) {
                        }
                    };
                }
            };
    private final DeleteCBProvider provideBucketCB;
    private final PermissionCache permissionCache;

    /**
     * Constructor sets up internal data source.
     * NOTE: the dependency on AccountManager may be injected later
     *     via setAccountManager to deal with circular dependency.
     *
     * @param sql_data_source
     * @param m_cache asset cache manager
     * @param m_retriever to delegate asset-retrieval to
     * @param m_db to obtain database controllers from
     * @param m_account for Quota ops. - may be null,
     *              but must inject later via setAccountManager
     */
    @Inject
    public SimpleAssetManager(
            CacheManager m_cache,
            AssetSearchManager m_search,
            DbAssetManager m_db,
            QuotaUtil quota,
            AssetSpecializerRegistry registry_special,
            Provider<LittleTransaction> provideTrans,
            littleware.apps.filebucket.server.DeleteCBProvider provideBucketCB,
            PermissionCache cachePermission) {
        cache = m_cache;
        search = m_search;
        dbMgr = m_db;
        quotaUtil = quota;
        specialRegistry = registry_special;
        this.provideTrans = provideTrans;
        this.provideBucketCB = provideBucketCB;
        this.permissionCache = cachePermission;
    }

    /** Internal utility */
    private LittleUser getAuthenticatedUser() throws NotAuthenticatedException {
        LittleUser user = SecurityAssetType.getAuthenticatedUserOrNull();

        if (null == user) {
            throw new NotAuthenticatedException("No user authenticated");
        }
        return user;
    }

    /**
     * Verify that the given string is properly formatted XML
     */
    public static boolean isValidXml(String s_xml) {
        // TODO: fill this in!!
        return true;
    }

    @Override
    public void deleteAsset(UUID u_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        try {
            final LittlePrincipal p_caller = this.getAuthenticatedUser();
            // Get the asset for ourselves - make sure it's a valid asset
            Asset asset = search.getAsset(u_asset).get();
            final AssetBuilder builder = asset.getAssetType().create().copy( asset );
            builder.setLastUpdateDate(new Date());
            builder.setLastUpdaterId(p_caller.getId());
            builder.setLastUpdate(s_update_comment);
            // make sure caller has write permission too ...
            asset = saveAsset(builder.build(), s_update_comment);

            final LittleTransaction trans_delete = provideTrans.get();
            boolean b_rollback = true;
            trans_delete.startDbUpdate();
            try {
                DbWriter<Asset> sql_writer = dbMgr.makeDbAssetDeleter();
                sql_writer.saveObject(asset);

                cache.remove(asset.getId());
                specialRegistry.getService(asset.getAssetType()).postDeleteCallback(asset, this);
                b_rollback = false;
                trans_delete.deferTillTransactionEnd(provideBucketCB.build(asset));
                final AssetType type = builder.getAssetType();
                if (type.isA(SecurityAssetType.ACL) || type.isA(SecurityAssetType.ACL_ENTRY) || type.isA(SecurityAssetType.GROUP) || type.isA(SecurityAssetType.GROUP_MEMBER)) {
                    permissionCache.clear();
                }
            } finally {
                trans_delete.endDbUpdate(b_rollback);
            }
        } catch (AssetException e) { // pass through
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Unexpected: " + e);
        }
    }

    @Override
    public <T extends Asset> T saveAsset(T asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        log.log(Level.FINE, "Check enter");
        final LittleUser userCaller = this.getAuthenticatedUser();
        // Get the asset for ourselves - make sure it's a valid asset
        Asset oldAsset = null;
        final AssetBuilder builder = asset.getAssetType().create().copy( asset );
        log.log(Level.FINE, "Check ready");
        if (null == asset.getName()) {
            throw new IllegalArgumentException("May not save an asset with a null name");
        }
        if (null == asset.getOwnerId()) {
            builder.setOwnerId(userCaller.getId());
        }
        if ((null == asset.getHomeId())) {
            if (asset.getAssetType().equals(AssetType.HOME)) {
                builder.setHomeId(asset.getId());
            } else {
                builder.setHomeId(userCaller.getHomeId());
            }
        }

        // Don't lookup the same asset more than once in this transaction
        final LittleTransaction trans_save = provideTrans.get();
        final Map<UUID, Asset> v_cache = trans_save.startDbAccess();
        // Don't save the same asset more than once in this transaction
        final Map<UUID, Asset> v_save_cycle = provideSaveCycle.get().startDbAccess();
        final boolean bCallerIsAdmin = permissionCache.isAdmin(userCaller, search);

        try {
            if (null == asset.getId()) {
                builder.setId(uuidFactory.create());
                if (asset.getAssetType().equals(AssetType.HOME)) {
                    // HOME asset type should reference itself
                    builder.setHomeId(asset.getId());
                }
            } else if (v_save_cycle.containsKey(asset.getId())) {
                log.log(Level.WARNING, "Save cycle detected - not saving " + asset);
                return asset;
            } else {
                oldAsset = search.getAsset(asset.getId()).getOr(null);
            }

            //olog_generic.log(Level.FINE, "Check pre-save");
            try {
                if (null == oldAsset) {
                    if (asset.getAssetType().isNameUnique() && search.getByName(asset.getName(), asset.getAssetType()).isSet()) {
                        throw new AlreadyExistsException("Asset of type " + asset.getAssetType() + " with name " + asset.getName() + " already exists");
                    }
                    // Check name-unique asset types
                    // creating a new asset
                    builder.setCreatorId(userCaller.getId());
                    // Check the caller's quota
                    if (v_save_cycle.isEmpty()) {
                        log.log(Level.FINE, "Incrementing quota before saving: " + asset);
                        quotaUtil.incrementQuotaCount(userCaller, this, search);
                    }
                    // Only allow admins to create new users and homes, etc.
                    if (asset.getAssetType().isAdminToCreate() && (!bCallerIsAdmin)) {
                        throw new AccessDeniedException("Must be in ADMIN group to create asset of type: " +
                                asset.getAssetType());
                    }
                } else {
                    // updating an existing asset
                    if (!oldAsset.getAssetType().equals(asset.getAssetType())) {
                        throw new AccessDeniedException("May not change asset type");
                    }
                    if ((!asset.getName().equals(oldAsset.getName())) && asset.getAssetType().isNameUnique()) {

                        if (search.getByName(asset.getName(), asset.getAssetType()).isSet()) {
                            throw new AlreadyExistsException("Asset of type " + asset.getAssetType() + " with name " + asset.getName() + " already exists");
                        }
                    }
                    if (!oldAsset.getCreatorId().equals(asset.getCreatorId())) {
                        throw new AccessDeniedException("May not change asset creator");
                    }
                    // 0 transaction count allows client to ignore serialization
                    if ((asset.getTransaction() > 0) && (oldAsset.getTransaction() > asset.getTransaction())) {
                        throw new AssetSyncException("Attempt to save asset not in sync with database backend: " + oldAsset);
                    }

                    log.log(Level.FINE, "Checking security");

                    if ((!bCallerIsAdmin) && (!oldAsset.getOwnerId().equals(userCaller.getId()))) {
                        // Need to have all the permissions to UPDATE an asset
                        if (!permissionCache.checkPermission(userCaller, LittlePermission.WRITE, search, oldAsset.getAclId())) {
                            throw new AccessDeniedException("Caller " + userCaller + " does not have permission: " + LittlePermission.WRITE + " for asset: " + oldAsset.getId());
                        }
                        if (!oldAsset.getOwnerId().equals(asset.getOwnerId())) {
                            throw new AccessDeniedException("Caller " + userCaller + " may not change owner on " +
                                    oldAsset.getId() + " unless he is the owner");
                        }
                        if (((oldAsset.getAclId() == null) && (asset.getAclId() != null)) || (!oldAsset.getAclId().equals(asset.getAclId()))) {
                            throw new AccessDeniedException("Caller " + userCaller +
                                    " may not change ACL on asset it does not own " +
                                    oldAsset.getId());
                        }
                    }
                }

                if (asset.getAssetType().equals(AssetType.HOME)) {
                    builder.setHomeId(asset.getId());
                } else {
                    log.log(Level.FINE, "Retrieving HOME");
                    final Asset a_home = search.getAsset(asset.getHomeId()).get();
                    log.log(Level.FINE, "Got HOME");
                    if (!a_home.getAssetType().equals(AssetType.HOME)) {
                        throw new HomeIdException("Home id must link to HOME type asset");
                    }
                    // If from-id is null from non-home orphan asset,
                    // then must have home-write permission to write home asset
                    if ((null == asset.getFromId()) && (!a_home.getOwnerId().equals(userCaller.getId())) && (!permissionCache.isAdmin(userCaller, search)) && (!permissionCache.checkPermission(userCaller, LittlePermission.WRITE, search, a_home.getAclId()))) {
                        // caller must have WRITE on Home permission to create a rootless
                        // (null from-id) asset
                        throw new AccessDeniedException("Must have home-write permission to create asset with null fromId");
                    }
                }

                if ((null != asset.getFromId()) && ((null == oldAsset) || (!asset.getFromId().equals(oldAsset.getFromId())))) {
                    log.log(Level.FINE, "Checking FROM-id access");
                    // Verify have WRITE access to from-asset, and under same HOME
                    final Asset a_from = search.getAsset(asset.getFromId()).get();

                    if ((!a_from.getOwnerId().equals(userCaller.getId())) && (!permissionCache.isAdmin(userCaller, search))) {
                        if (!permissionCache.checkPermission(userCaller, LittlePermission.WRITE, search, a_from.getAclId())) {
                            throw new AccessDeniedException("Caller " + userCaller +
                                    " may not link from asset " + a_from.getId() +
                                    " without permission " + LittlePermission.WRITE);
                        }
                    }
                    if ((!a_from.getHomeId().equals(asset.getHomeId()))) {
                        throw new HomeIdException("May not link FROM an asset with a different HOME");
                    }
                    if (a_from.getAssetType().equals(AssetType.LINK)) {
                        throw new FromLinkException("May not link FROM an asset of type AssetType.LINK");
                    }
                }
                if (null != asset.getToId()) {
                    // Verify have READ access to to-asset - rely on om_retriever security check
                    search.getAsset(asset.getToId());
                }

                builder.setLastUpdateDate(new Date());
                builder.setLastUpdaterId(userCaller.getId());
                builder.setLastUpdate(s_update_comment);

                boolean b_rollback = true;
                trans_save.startDbUpdate();
                
                try {
                    final Asset assetSave = builder.build();
                    final DbWriter<Asset> sql_writer = dbMgr.makeDbAssetSaver();
                    sql_writer.saveObject(assetSave);
                    cache.put(assetSave.getId(), assetSave);

                    v_save_cycle.put(assetSave.getId(), assetSave);
                    v_cache.put(assetSave.getId(), assetSave);

                    if (null == oldAsset) {
                        specialRegistry.getService(assetSave.getAssetType()).postCreateCallback(assetSave, this);
                    } else {
                        specialRegistry.getService(assetSave.getAssetType()).postUpdateCallback(oldAsset, assetSave, this);
                    }

                    final AssetType type = assetSave.getAssetType();
                    if (type.isA(SecurityAssetType.ACL) || type.isA(SecurityAssetType.ACL_ENTRY) || type.isA(SecurityAssetType.GROUP) || type.isA(SecurityAssetType.GROUP_MEMBER)) {
                        permissionCache.clear();
                    }

                    b_rollback = false;
                    return (T) assetSave;
                } finally {
                    trans_save.endDbUpdate(b_rollback);
                }
            } catch (SQLException e) {
                // Should check SQLException error-string for specific error translation here ...
                // Do not throw SQLException to client - may not be serializable
                if (e.toString().indexOf("littleware(sync)") >= 0) {
                    throw new AssetSyncException("Attempt to save asset not in sync with database backend");
                }
                throw new DataAccessException("Unexpected: " + e);
            }

        } finally {
            provideSaveCycle.get().endDbAccess(v_save_cycle);
            trans_save.endDbAccess(v_cache);
        }
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(Collection<Asset> v_assets, String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        final LittleTransaction trans_batch = provideTrans.get();
        boolean b_rollback = true;

        final List<Asset> result = new ArrayList<Asset>();

        trans_batch.startDbUpdate();
        try {
            for (Asset a_save : v_assets) {
                result.add(saveAsset(a_save, s_update_comment));
            }
            b_rollback = false;
        } finally {
            trans_batch.endDbUpdate(b_rollback);
        }

        return result;
    }
}
