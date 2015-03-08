/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;

import java.security.GeneralSecurityException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

//import littleware.apps.filebucket.server.DeleteCBProvider;
import littleware.asset.*;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerSearchManager;
import littleware.security.server.QuotaUtil;
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
public class SimpleAssetManager implements ServerAssetManager {

    private static final Logger log = Logger.getLogger(SimpleAssetManager.class.getName());
    private final DbAssetManager dbMgr;
    private final ServerSearchManager search;
    private final Provider<UUID> uuidFactory;
    private final QuotaUtil quotaUtil;
    private final AssetSpecializerRegistry specializerReg;

    /**
     * Constructor injects dependencies.
     */
    @Inject
    public SimpleAssetManager(
            ServerSearchManager search,
            DbAssetManager dbMgr,
            QuotaUtil quotaUtil,
            AssetSpecializerRegistry specializerReg,
            Provider<UUID> uuidFactory ) {
        this.search = search;
        this.dbMgr = dbMgr;
        this.quotaUtil = quotaUtil;
        this.specializerReg = specializerReg;
        this.uuidFactory = uuidFactory;
    }
    

    @Override
    public void deleteAsset(LittleContext ctx, UUID assetId,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException {
        try {
            // make sure caller has write permission too ...
            final LittleTransaction trans = ctx.getTransaction();
            trans.startDbAccess();
            try {
                final LittlePrincipal caller = ctx.getCaller();
                log.log( Level.FINE, "Deleting asset with id {0} - trans level {1}", new Object[]{assetId, trans.getNestingLevel()});
                // Get the asset for ourselves - make sure it's a valid asset
                Asset asset = search.getAsset(ctx, assetId, -1L ).getAsset().get();
                final AssetBuilder builder = asset.copy();
                builder.setLastUpdateDate(new Date());
                builder.setLastUpdaterId(caller.getId());
                builder.setLastUpdate(updateComment);
                
                boolean rollback = true;
                trans.startDbUpdate();
                log.log( Level.FINE, "Trans level 2: {0}", trans.getNestingLevel());
                try {
                    asset = saveAsset(ctx, builder.build(), updateComment).get( asset.getId() );
                    log.log( Level.FINE, "Trans level 3: {0}", trans.getNestingLevel());
                    if ( null == asset ) { 
                        throw new AssertionFailedException( "pre-delete save result map does not include saved asset: " + asset.getId() );
                    }
                    final DbWriter<Asset> sql_writer = dbMgr.makeDbAssetDeleter(trans);
                    sql_writer.saveObject(asset);

                    specializerReg.getService(asset.getAssetType()).postDeleteCallback(ctx, asset);
                    trans.getCache().remove(asset.getId());
                    rollback = false;
                    //trans_delete.deferTillTransactionEnd(provideBucketCB.build(asset));
                    /*..
                    final AssetType type = builder.getAssetType();
                    
                    if (type.isA(LittleAcl.ACL_TYPE) || type.isA(LittleAclEntry.ACL_ENTRY) || type.isA(LittleGroup.GROUP_TYPE) || type.isA(LittleGroupMember.GROUP_MEMBER_TYPE)) {
                    permissionCache.clear();
                    }
                     *
                     */
                } finally {
                    log.log( Level.FINE, "Trans level 4: {0}", trans.getNestingLevel());
                    trans.endDbUpdate(rollback);
                }
            } finally {
                trans.endDbAccess();
            }
        } catch (AssetException ex) { // pass through
            throw ex;
        } catch (SQLException ex) {
            // do not chain SQL Exception - remote client may not have the class loaded
            log.log(Level.INFO, "Failed call", ex);
            throw new DataAccessException("Unexpected: " + ex);
        }
    }

    @Override
    public ImmutableMap<UUID,Asset> saveAsset(LittleContext ctx, Asset asset,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException {
        log.log(Level.FINE, "Check enter");
        final LittleUser userCaller = ctx.getCaller();
        // Get the asset for ourselves - make sure it's a valid asset
        Asset oldAsset = null;
        final AssetBuilder builder = asset.copy();
        builder.validate();  // sometimes implementations forget to validate() on build() ... doh!
        log.log(Level.FINE, "Check ready");
        if (null == asset.getName()) {
            throw new IllegalArgumentException("May not save an asset with a null name");
        }
        if (null == asset.getOwnerId()) {
            builder.setOwnerId(userCaller.getId());
        }
        if ((null == asset.getHomeId())) {
            if (asset.getAssetType().equals(LittleHome.HOME_TYPE)) {
                builder.setHomeId(asset.getId());
            } else {
                builder.setHomeId(userCaller.getHomeId());
            }
        }

        // Don't lookup the same asset more than once in this timestamp
        final LittleTransaction trans = ctx.getTransaction();
        final int startTransLevel = trans.getNestingLevel();
        
        // Don't save the same asset more than once in this timestamp
        final boolean bCallerIsAdmin = ctx.isAdmin();
        final boolean cycleSave;  // is this a save via a callback ?  - avoid infinite loops
        final Map<UUID,Asset>  resultBuilder = new HashMap<>();
        
        final Map<UUID, Asset> accessCache = trans.startDbAccess();
        try {
            if (null == asset.getId()) {
                builder.setId(uuidFactory.get());
                if (asset.getAssetType().equals(LittleHome.HOME_TYPE)) {
                    // HOME asset type should reference itself
                    builder.setHomeId(asset.getId());
                }
                cycleSave = false;
            } else if (ctx.checkIfSaved(asset.getId()).isSet()) {
                //log.log(Level.WARNING, "Save cycle detected - not saving " + asset);
                //return asset;
                oldAsset = ctx.checkIfSaved(asset.getId()).get();
                cycleSave = true;
            } else {
                oldAsset = search.getAsset(ctx, asset.getId(), -1L).getAsset().getOr(null);
                cycleSave = false;
            }

            //olog_generic.log(Level.FINE, "Check pre-save");
            try {
                if (null == oldAsset) {
                    if (asset.getAssetType().isNameUnique() && search.getByName(ctx, asset.getName(), asset.getAssetType()).isSet()) {
                        throw new AlreadyExistsException("Asset of type " + asset.getAssetType() + " with name " + asset.getName() + " already exists");
                    }
                    // Check name-unique asset types
                    // creating a new asset
                    if ((null == builder.getCreatorId()) || (!bCallerIsAdmin)) {
                        builder.setCreatorId(userCaller.getId());
                    }
                    /*... disable quota stuff for now ...
                    // Check the caller's quota
                    if (v_save_cycle.isEmpty()) {
                    log.log(Level.FINE, "Incrementing quota before saving: {0}", asset);
                    quotaUtil.incrementQuotaCount(userCaller, this, search);
                    }
                     *
                     */
                    // Only allow admins to create new users and homes, etc.
                    if (asset.getAssetType().isAdminToCreate() && (!bCallerIsAdmin)) {
                        throw new AccessDeniedException("Must be in ADMIN group to create asset of type: "
                                + asset.getAssetType());
                    }
                } else {
                    // updating an existing asset
                    if (!oldAsset.getAssetType().equals(asset.getAssetType())) {
                        throw new AccessDeniedException("May not change asset type");
                    }
                    if ((!asset.getName().equals(oldAsset.getName())) && asset.getAssetType().isNameUnique()) {

                        if (search.getByName(ctx, asset.getName(), asset.getAssetType()).isSet()) {
                            throw new AlreadyExistsException("Asset of type " + asset.getAssetType() + " with name " + asset.getName() + " already exists");
                        }
                    }

                    builder.creatorId( oldAsset.getCreatorId() 
                        ).createDate( oldAsset.getCreateDate() );
                    
                    // 0 timestamp count allows client to ignore serialization
                    if ((asset.getTimestamp() > 0) && (oldAsset.getTimestamp() > asset.getTimestamp())) {
                        throw new AssetSyncException("Attempt to save asset not in sync with database backend: " + oldAsset
                                + ", " + oldAsset.getTimestamp() + " gt " + asset.getTimestamp());
                    }

                    log.log(Level.FINE, "Checking security");

                    if ((!bCallerIsAdmin) && (!oldAsset.getOwnerId().equals(userCaller.getId()))) {
                        // Need to have all the permissions to UPDATE an asset
                        if (!ctx.checkPermission(LittlePermission.WRITE, oldAsset.getAclId())) {
                            throw new AccessDeniedException("Caller " + userCaller + " does not have permission: " + LittlePermission.WRITE + " for asset: " + oldAsset.getId());
                        }
                        if (!oldAsset.getOwnerId().equals(asset.getOwnerId())) {
                            throw new AccessDeniedException("Caller " + userCaller + " may not change owner on "
                                    + oldAsset.getId() + " unless he is the owner");
                        }
                        if (((oldAsset.getAclId() == null) && (asset.getAclId() != null)) || (!oldAsset.getAclId().equals(asset.getAclId()))) {
                            throw new AccessDeniedException("Caller " + userCaller
                                    + " may not change ACL on asset it does not own "
                                    + oldAsset.getId());
                        }
                    }
                }

                if (asset.getAssetType().equals(LittleHome.HOME_TYPE)) {
                    builder.setHomeId(asset.getId());
                } else {
                    log.log(Level.FINE, "Retrieving HOME");
                    final Asset home = search.getAsset(ctx, asset.getHomeId(), -1L ).getAsset().get();
                    log.log(Level.FINE, "Got HOME");
                    if (!home.getAssetType().isA(LittleHome.HOME_TYPE)) {
                        throw new IllegalArgumentException("Home id must link to HOME type asset");
                    }
                    // If from-id is null from non-home orphan asset,
                    // then must have home-write permission to write home asset
                    if (null == asset.getFromId()) { //&& (!a_home.getOwnerId().equals(userCaller.getId())) && (!permissionCache.isAdmin(userCaller, search)) && (!permissionCache.checkPermission(userCaller, LittlePermission.WRITE, search, a_home.getAclId()))) {
                        // caller must have WRITE on Home permission to create a rootless
                        // (null from-id) asset
                        throw new AccessDeniedException("Must have home-write permission to create asset with null fromId");
                    }
                }

                if ((null != asset.getFromId()) && ((null == oldAsset) || (!asset.getFromId().equals(oldAsset.getFromId())))) {
                    log.log(Level.FINE, "Checking FROM-id access");
                    // Verify have WRITE access to from-asset, and under same HOME
                    final Asset parent = search.getAsset(ctx, asset.getFromId(), -1L ).getAsset().get();

                    if ((!parent.getOwnerId().equals(userCaller.getId())) && (!ctx.isAdmin())) {
                        if (!ctx.checkPermission(LittlePermission.WRITE, parent.getAclId())) {
                            throw new AccessDeniedException("Caller " + userCaller
                                    + " may not link from asset " + parent.getId()
                                    + " without permission " + LittlePermission.WRITE);
                        }
                    }
                    if ((!parent.getHomeId().equals(asset.getHomeId()))) {
                        throw new IllegalArgumentException("May not link FROM an asset with a different HOME");
                    }
                    if (parent.getAssetType().equals(LinkAsset.LINK_TYPE)) {
                        throw new IllegalArgumentException("May not link FROM an asset of type LinkAsset.LINK_TYPE");
                    }
                    if ( search.getAssetIdsFrom( ctx, parent.getId() ).containsKey( asset.getName() ) ) {
                        throw new IllegalArgumentException( "Parent already has child with name: " + asset.getName() );
                    }
                }

                builder.setLastUpdateDate(new Date());
                builder.setLastUpdaterId(userCaller.getId());
                builder.setLastUpdate(updateComment);

                boolean rollback = true;
                trans.startDbUpdate();

                final Asset assetSave;
                try {
                    assetSave = builder.timestamp(trans.getTimestamp()).build();
                    final AssetSpecializer specializer = specializerReg.getService(assetSave.getAssetType());
                    for( String problem : specializer.validate( ctx, assetSave)) {
                        throw new IllegalArgumentException( "Failed validation: " + problem );
                    }

                    final DbWriter<Asset> sql_writer = dbMgr.makeDbAssetSaver(trans);
                    sql_writer.saveObject(assetSave);
                    ctx.savedAsset(assetSave);
                    accessCache.put(assetSave.getId(), assetSave);
                    
                    if (null == oldAsset) {
                        for( Asset scan : specializer.postCreateCallback(ctx, assetSave) ) {
                            resultBuilder.put( scan.getId(), scan);
                        }
                    } else if (!cycleSave) { // do not make multiple callbacks on same asset
                        for( Asset scan : specializer.postUpdateCallback(ctx, oldAsset, assetSave) ) {
                            resultBuilder.put( scan.getId(), scan);
                        }
                    } else {
                        log.log(Level.FINE, "Bypassing save-callback on cycle-save asset {0}/{1}/{2}", new Object[]{assetSave.getId(), assetSave.getAssetType(), assetSave.getName()});
                    }

                    final AssetType type = assetSave.getAssetType();
                    if (type.isA(LittleAcl.ACL_TYPE) || type.isA(LittleAclEntry.ACL_ENTRY) || type.isA(LittleGroup.GROUP_TYPE) || type.isA(LittleGroupMember.GROUP_MEMBER_TYPE)) {
                        //permissionCache.clear();
                    }

                    rollback = false;
                } finally {
                    try {
                        trans.endDbUpdate(rollback);
                    } catch (Exception ex2) {
                        log.log(Level.INFO, "Eating rollback exception", ex2);
                    }
                }
                { // this should hit the transaction access-cache
                    final Asset result = search.getAsset(ctx, assetSave.getId(), -1L ).getAsset().get();
                    resultBuilder.put( result.getId(), result );
                    return ImmutableMap.copyOf(resultBuilder);
                }
            } catch (Throwable ex) {
                // Should check SQLException error-string for specific error translation here ...
                // Do not propagate database exception to client - may not be serializable
                if (ex.toString().indexOf("littleware(sync)") >= 0) {
                    throw new AssetSyncException("Attempt to save asset not in sync with database backend");
                }
                log.log(Level.INFO, "Save failed", ex);
                if (ex instanceof RuntimeException) {
                    throw (RuntimeException) ex;
                }
                throw new DataAccessException("Unexpected: " + ex);
            }

        } finally {
            trans.endDbAccess(accessCache);
            Whatever.get().check( "Transaction level consistent: " + startTransLevel + " =? " + trans.getNestingLevel(),
                    startTransLevel == trans.getNestingLevel()
            );
        }
    }

    @Override
    public ImmutableMap<UUID,Asset> saveAssetsInOrder(LittleContext ctx, Collection<Asset> assetList, String updateComment) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        boolean rollback = true;

        final ImmutableMap.Builder<UUID,Asset> resultBuilder = ImmutableMap.builder();

        trans.startDbUpdate();
        try {
            for (Asset scan : assetList) {
                resultBuilder.putAll(saveAsset(ctx, scan, updateComment));
            }
            rollback = false;
        } finally {
            trans.endDbUpdate(rollback);
        }

        return resultBuilder.build();
    }
}
