/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

import java.util.*;
import java.util.logging.Logger;
import java.security.AccessController;
import java.security.Permission;

import littleware.base.*;

/**
 * Enumerate different types of assets, and provide
 * factory methods to create appropriate (uninitialized)
 * Asset specialization type.
 * We implement our own Enum pattern, since we want
 * to be able to dynamically add members to the Enum.
 * A user should be able to introduce a new AssetType to the
 * system by registering a new entry in the database,
 * and adding code to the classpath that defines the
 * AssetType and AssetSpecializer for that new type.
 * Our enum's are uniquely identified by UUID, not by
 * integer ordinal - so we don't have to worry about 
 * 2 users in different locations assigning the same ID to 
 * 2 different types.
 * Subtypes should attempt to maintain themselves as singletons locally,
 * but also tolerate the serialization/deserialization of new instances
 * due to RPC/etc.
 *
 * Note: subtypes implementing AssetType must be in a code-base
 *     granted AccessPermission "littleware.asset.resource.newtype"
 */
public abstract class AssetType<T extends Asset> extends DynamicEnum<AssetType> implements Factory<T> {
    private static final long serialVersionUID = 1111142L;
    private static final Logger olog_generic = Logger.getLogger(AssetType.class.getName() );

    /**
     * Do-nothing constructor intended for deserialization only.
     * Does a 'newtype' permission check to make sure an RMI caller
     * does not inject a new AssetType into our server.
     */
    protected AssetType() {
        Permission perm_newtype = new AccessPermission("newtype");
        AccessController.checkPermission(perm_newtype);
    }

    /**
     * Constructor for subtypes to register a u_id/s_name
     * for the default implementation of getObjectId() and getName()
     */
    protected AssetType(UUID u_id, String s_name) {
        super(u_id, s_name, AssetType.class, new AccessPermission("newtype"));
    }

    /** Shortcut to DynamicEnum.getMembers */
    public static Set<AssetType> getMembers() {
        return getMembers(AssetType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static AssetType getMember(UUID u_id) throws NoSuchTypeException {
        try {
            return getMember(u_id, AssetType.class);
        } catch (NoSuchThingException e) {
            throw new NoSuchTypeException("No asset type: " + u_id, e);
        }

    }

    /** Shortcut to DynamicEnum.getMember */
    public static AssetType getMember(String s_name) throws NoSuchTypeException {
        try {
            return getMember(s_name, AssetType.class);
        } catch (NoSuchThingException e) {
            throw new NoSuchTypeException("No asset type: " + s_name, e);
        }
    }

    /** Default factory method just returns a SimpleAsset */
    public abstract T create() throws FactoryException;

    /**
     * Does littleware enforce name-uniqueness (on a given server cluster) 
     * for this asset-type ?
     * A false result indicates that two assets
     * of this type may have the same name.
     * This base class method returns:
     *         (null == getSuperType()) ? false : getSuperType ().isNameUnique ()
     *
     * @return true if each asset of this type has a unique name within the
     *          set of assets of this type within a single littleware asset repository.
     */
    public boolean isNameUnique() {
        return (null == getSuperType()) ? false : getSuperType().isNameUnique();
    }

    /**
     * Allow an asset-type to have one super type.
     * Methods that search based on AssetType, like AssetSearchManager.getByName,
     * will return objects assets matching the given type and
     * any of its subtypes.
     *
     * @return super-type, or null if no super-type (this implementation returns null)
     */
    public AssetType<? extends Asset> getSuperType() {
        return null;
    }

    /**
     * Is this asset-type a subtype of the given asset-type ?
     * Note that if x isNameUnique, then all subtypes of x must
     * by nameUnique too - or you'll run into problems.
     *
     * NOTE: AssetTypes do not share a common ancestor (like Object) -
     *     most search routines just use NULL as a wildcard asset-type
     */
    public final boolean isA(AssetType atype_other) {
        if ((null == this) || (null == atype_other)) {
            return false;
        }

        // climb this assset's inheritance tree, cache the result
        for (AssetType atype_check = this;
                atype_check != null;
                atype_check = atype_check.getSuperType()) {
            if (atype_check.equals(atype_other)) {
                return true;
            }
        }
        return false;
    }

  
    /** Noop recycle method to satisfy Factory interface */
    public void recycle(T a_whatever) {
    }

    /**
     * Whether a user must be in the ADMIN group to create this type of asset.
     * This base class method returns:
     *         (null == getSuperType()) ? false : getSuperType ().mustBeAdminToCreate ()
     */
    public boolean mustBeAdminToCreate() {
        return ((null == getSuperType()) ? false : getSuperType().mustBeAdminToCreate());
    }
    /** GENERIC asset-type */
    public static final AssetType<Asset> GENERIC = new AssetType<Asset>(UUIDFactory.parseUUID("E18D1B19D9714F6F8F49CF9B431EBF23"),
            "littleware.GENERIC") {

        public Asset create() {
            Asset a_result = new SimpleAsset();
            a_result.setAssetType(this);
            return a_result;
        }
    };
    /** HOME asset-type - must be admin to create */
    public static final AssetType<Asset> HOME = new AssetType<Asset>(UUIDFactory.parseUUID("C06CC38C6BD24D48AB5E2D228612C179"),
            "littleware.HOME") {

        public Asset create() {
            Asset a_result = new SimpleAsset();
            a_result.setAssetType(this);
            return a_result;
        }

        /** Always return true */
        public boolean mustBeAdminToCreate() {
            return true;
        }

        /** Always return true */
        public boolean isNameUnique() {
            return true;
        }
    };
    /** LINK assset-type */
    public static final AssetType<Asset> LINK = new AssetType<Asset>(UUIDFactory.parseUUID("926D122F82FE4F28A8F5C790E6733665"),
            "littleware.LINK") {

        public Asset create() {
            Asset a_result = new SimpleAsset();
            a_result.setAssetType(this);
            return a_result;
        }
    };
    /**
     * UNKNOWN asset-type - place holder for asset-types that we don't have a handler for.
     * The engine refuses to save/create/update assets with UNKNOWN type.
     */
    public static final AssetType<Asset> UNKNOWN = new AssetType<Asset>(UUIDFactory.parseUUID("EDC97D5F816044E69BFC289F4715BA45"),
            "littleware.UNKNOWN") {

        public Asset create() {
            Asset a_result = new SimpleAsset();
            a_result.setAssetType(this);
            return a_result;
        }
    };

    /**
     * Utility creates an asset with the given type and name
     * linking FROM the given parent asset, and intialized
     * with the same HOME and ACL as the parent.
     *
     * @param atype_new type of asset to create
     * @param s_name to give the new asset
     * @param a_parent asset to link from and get HOME, and ACL from.
     * @exception FactoryException if propagated by atype_new.create
     */
    public static <T extends Asset> T createSubfolder(AssetType<T> atype_new, String s_name,
            Asset a_parent) throws FactoryException {
        T a_new = atype_new.create();
        a_new.setName(s_name);
        a_new.setFromId(a_parent.getObjectId());
        a_new.setHomeId(a_parent.getHomeId());
        a_new.setAclId(a_parent.getHomeId());
        return a_new;
    }
}

