package littleware.asset.server;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.*;
import java.security.acl.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

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

    private static final Logger olog_generic = Logger.getLogger("littleware.asset.server.SimpleAssetManager");
    private static final ThreadLocal<LittleTransaction> othread_save_cycle = new ThreadLocal<LittleTransaction>() {

        @Override
        protected LittleTransaction initialValue() {
            return new SimpleLittleTransaction();
        }
    };
    private final DbAssetManager om_db;
    private final CacheManager om_cache;
    private final AssetSearchManager om_search;
    private final Factory<UUID> ofactory_uuid = UUIDFactory.getFactory();
    private final QuotaUtil     oquota;
    private final AssetSpecializerRegistry  oregistry_special;

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
            AssetSpecializerRegistry  registry_special
            ) {
        om_cache = m_cache;
        om_search = m_search;
        om_db = m_db;
        oquota = quota;
        oregistry_special = registry_special;
    }

    /** Internal utility */
    private LittleUser getAuthenticatedUser() throws NotAuthenticatedException {
        LittleUser p_result = SecurityAssetType.getAuthenticatedUserOrNull();

        if (null == p_result) {
            throw new NotAuthenticatedException("No user authenticated");
        }
        return p_result;
    }


    /**
     * Verify that the given string is properly formatted XML
     */
    public static boolean isValidXml(String s_xml) {
        // TODO: fill this in!!
        return true;
    }

    public void deleteAsset(UUID u_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        try {
            LittlePrincipal p_caller = this.getAuthenticatedUser();
            // Get the asset for ourselves - make sure it's a valid asset
            Asset a_asset = om_search.getAsset(u_asset);
            Owner o_asset = a_asset.getOwner(om_search);
            // add security later
            if (false && (!o_asset.isOwner(p_caller))) {
                if (null == a_asset.getAclId()) {
                    throw new AccessDeniedException("may not delete asset without permission: " +
                            a_asset.getObjectId() + "/" + a_asset.getName());
                }
                Acl acl_x = a_asset.getAcl(om_search);

                // Need to have all the permissions to DELETE an asset
                for (LittlePermission n_permission : LittlePermission.getMembers()) {
                    if (!acl_x.checkPermission(p_caller, n_permission)) {
                        throw new AccessDeniedException("Caller " + p_caller + " does not have permission: " + n_permission);
                    }
                }
            }

            a_asset.setLastUpdateDate(new Date());
            a_asset.setLastUpdaterId(p_caller.getObjectId());
            a_asset.setLastUpdate(s_update_comment);

            LittleTransaction trans_delete = TransactionManager.getTheThreadTransaction();
            boolean b_rollback = true;
            trans_delete.startDbUpdate();
            try {
                DbWriter<Asset> sql_writer = om_db.makeDbAssetDeleter();
                sql_writer.saveObject(a_asset);

                om_cache.remove(a_asset.getObjectId());
                oregistry_special.getService( a_asset.getAssetType() ).postDeleteCallback(a_asset, this);
                b_rollback = false;
            } finally {
                trans_delete.endDbUpdate(b_rollback);
            }
        } catch (AssetException e) { // pass through
            throw e;
        } catch (SQLException e) {
            throw new DataAccessException("Unexpected: " + e);
        }
    }

    public <T extends Asset> T saveAsset(T a_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        olog_generic.log(Level.FINE, "Check enter");
        LittleUser p_caller = this.getAuthenticatedUser();
        // Get the asset for ourselves - make sure it's a valid asset
        Asset a_old_asset = null;

        olog_generic.log(Level.FINE, "Check ready");
        if (null == a_asset.getName()) {
            throw new IllegalArgumentException("May not save an asset with a null name");
        }
        if (null == a_asset.getOwnerId()) {
            a_asset.setOwnerId(p_caller.getObjectId());
        }
        if (null == a_asset.getHomeId()) {
            a_asset.setHomeId(p_caller.getHomeId());
        }

        // Don't lookup the same asset more than once in this transaction
        LittleTransaction trans_save = TransactionManager.getTheThreadTransaction();
        Map<UUID, Asset> v_cache = trans_save.startDbAccess();
        // Don't save the same asset more than once in this transaction
        Map<UUID, Asset> v_save_cycle = othread_save_cycle.get().startDbAccess();

        try {
            if (null == a_asset.getObjectId()) {
                try {
                    a_asset.setObjectId(ofactory_uuid.create());
                } catch (FactoryException e) {
                    throw new AssertionFailedException("Failed to create new UUID, caught: " + e, e);
                }
            } else if (v_save_cycle.containsKey(a_asset.getObjectId())) {
                olog_generic.log(Level.WARNING, "Save cycle detected - not saving " + a_asset);
                return a_asset;
            } else {
                a_old_asset = om_search.getAssetOrNull(a_asset.getObjectId());
            }

            olog_generic.log(Level.FINE, "Check pre-save");
            try {
                if (null == a_old_asset) {
                    a_asset.setCreatorId(p_caller.getObjectId());
                    // Check the caller's quota
                    if (v_save_cycle.isEmpty()) {
                        olog_generic.log(Level.FINE, "Incrementing quota before saving: " + a_asset);
                        oquota.incrementQuotaCount( p_caller, this, om_search );
                    }
                    // Only allow admins to create new users and homes, etc.
                    if (a_asset.getAssetType().mustBeAdminToCreate()) {
                        LittleGroup p_admin_group = (LittleGroup) om_search.getAsset(
                                AccountManager.UUID_ADMIN_GROUP);
                        if (!p_admin_group.isMember(p_caller)) {
                            throw new AccessDeniedException("Must be in ADMIN group to create asset of type: " +
                                    a_asset.getAssetType());
                        }
                    }
                } else {
                    if (!a_old_asset.getAssetType().equals(a_asset.getAssetType())) {
                        throw new AccessDeniedException("May not change asset type");
                    }
                    if (!a_old_asset.getCreatorId().equals(a_asset.getCreatorId())) {
                        throw new AccessDeniedException("May not change asset creator");
                    }
                    // 0 transaction count allows client to ignore serialization
                    if ((a_asset.getTransactionCount() > 0) && (a_old_asset.getTransactionCount() > a_asset.getTransactionCount())) {
                        throw new AssetSyncException("Attempt to save asset not in sync with database backend: " + a_old_asset);
                    }

                    olog_generic.log(Level.FINE, "Checking security");

                    Owner o_old = a_old_asset.getOwner(om_search);
                    if (!o_old.isOwner(p_caller)) {
                        Acl acl_x = a_old_asset.getAcl(om_search);

                        // Need to have all the permissions to UPDATE an asset
                        if (!acl_x.checkPermission(p_caller, LittlePermission.WRITE)) {
                            throw new AccessDeniedException("Caller " + p_caller + " does not have permission: " 
                                    + LittlePermission.WRITE + " for asset: " + a_old_asset.getObjectId()
                                    );
                        }
                        if (!a_old_asset.getOwnerId().equals(a_asset.getOwnerId())) {
                            throw new AccessDeniedException("Caller " + p_caller + " may not change owner on " +
                                    a_old_asset.getObjectId() + " unless he is the owner");
                        }
                        if (((a_old_asset.getAclId() == null) && (a_asset.getAclId() != null)) || (!a_old_asset.getAclId().equals(a_asset.getAclId()))) {
                            throw new AccessDeniedException("Caller " + p_caller +
                                    " may not change ACL on asset it does not own " +
                                    a_old_asset.getObjectId());
                        }
                    }
                }

                olog_generic.log(Level.FINE, "Retrieving HOME");
                Asset a_home = om_search.getAsset(a_asset.getHomeId());
                olog_generic.log(Level.FINE, "Got HOME");
                if (!a_home.getAssetType().equals(AssetType.HOME)) {
                    throw new HomeIdException("Home id must link to HOME type asset");
                }

                if ((null != a_asset.getFromId()) && ((null == a_old_asset) || (!a_asset.getFromId().equals(a_old_asset.getFromId())))) {
                    olog_generic.log(Level.FINE, "Checking FROM-id access");
                    // Verify have WRITE access to from-asset, and under same HOME
                    Asset a_from = om_search.getAsset(a_asset.getFromId());

                    Owner o_from = a_from.getOwner(om_search);
                    if (!o_from.isOwner(p_caller)) {
                        Acl acl_from = a_from.getAcl(om_search);
                        if ((null == acl_from) || (!acl_from.checkPermission(p_caller, LittlePermission.WRITE))) {
                            throw new AccessDeniedException("Caller " + p_caller +
                                    " may not link from asset " + a_from.getObjectId() +
                                    " without permission " + LittlePermission.WRITE);
                        }
                    }
                    if ((!a_from.getHomeId().equals(a_asset.getHomeId()) && (!a_asset.getHomeId().equals(a_asset.getFromId())) // linking FROM HOME
                            )) {
                        throw new HomeIdException("May not link FROM an asset with a different HOME");
                    }
                    if (a_from.getAssetType().equals(AssetType.LINK)) {
                        throw new FromLinkException("May not link FROM an asset of type AssetType.LINK");
                    }
                }
                if (null != a_asset.getToId()) {
                    // Verify have READ access to to-asset - rely on om_retriever security check
                    Asset a_to = om_search.getAsset(a_asset.getToId());
                }

                a_asset.setLastUpdateDate(new Date());
                a_asset.setLastUpdaterId(p_caller.getObjectId());
                a_asset.setLastUpdate(s_update_comment);

                boolean b_rollback = true;
                trans_save.startDbUpdate();
                try {
                    DbWriter<Asset> sql_writer = om_db.makeDbAssetSaver();
                    sql_writer.saveObject(a_asset);
                    om_cache.put(a_asset.getObjectId(), a_asset);


                    v_save_cycle.put(a_asset.getObjectId(), a_asset);
                    v_cache.put(a_asset.getObjectId(), a_asset);

                    if (null == a_old_asset) {
                        oregistry_special.getService( a_asset.getAssetType() ).postCreateCallback(a_asset, this);
                    } else {
                        oregistry_special.getService( a_asset.getAssetType() ).postUpdateCallback(a_old_asset, a_asset, this);
                    }
                    b_rollback = false;
                } finally {
                    trans_save.endDbUpdate(b_rollback);
                }
            } catch (SQLException e) {
                // Should check SQLException error-string for specific error translation here ...
                if (e.toString().indexOf("littleware(sync)") >= 0) {
                    throw new AssetSyncException("Attempt to save asset not in sync with database backend", e);
                }
                throw new DataAccessException("Unexpected: " + e, e);
            }

        } finally {
            othread_save_cycle.get().endDbAccess(v_save_cycle);
            TransactionManager.getTheThreadTransaction().endDbAccess(v_cache);
        }
        return a_asset;
    }

    public Collection<Asset> saveAssetsInOrder(Collection<Asset> v_assets, String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        LittleTransaction trans_batch = TransactionManager.getTheThreadTransaction();
        boolean b_rollback = true;
        try {
            List<Asset> v_result = new ArrayList<Asset>();

            trans_batch.startDbUpdate();
            try {
                for (Asset a_save : v_assets) {
                    v_result.add(saveAsset(a_save, s_update_comment));
                }
                b_rollback = false;
            } finally {
                trans_batch.endDbUpdate(b_rollback);
            }

            return v_result;
        } catch (SQLException e) {
            throw new DataAccessException("Unexpected SQLException", e);
        }

    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

