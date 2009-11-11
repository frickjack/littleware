/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
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
public abstract class AssetType extends DynamicEnum<AssetType> {
    private static final long serialVersionUID = 1111142L;
    private static final Logger log = Logger.getLogger(AssetType.class.getName() );

    /**
     * Do-nothing constructor intended for deserialization only.
     */
    protected AssetType() {
        //final Permission permission = new AccessPermission("newtype");
        //AccessController.checkPermission(permission);
    }

    /**
     * Constructor for subtypes to register a u_id/s_name
     * for the default implementation of getObjectId() and getName()
     */
    protected AssetType(UUID u_id, String s_name) {
        super(u_id, s_name, AssetType.class);
    }

    /** Default factory method just returns a SimpleAssetBuilder */
    public AssetBuilder create() {
        return new SimpleAssetBuilder( this );
    }

    public static abstract class Specialized<T extends AssetBuilder> extends AssetType {
        public Specialized( UUID id, String name ) {
            super( id, name );
        }

        @Override
        public T create() {
            return (T) super.create();
        }
    }

    
    /** Shortcut to DynamicEnum.getMembers */
    public static Set<AssetType> getMembers() {
        return getMembers(AssetType.class);
    }

    /** Shortcut to DynamicEnum.getMember */
    public static AssetType getMember(UUID id) throws NoSuchTypeException {
        try {
            return getMember(id, AssetType.class);
        } catch (NoSuchThingException e) {
            throw new NoSuchTypeException("No asset type: " + id, e);
        }

    }

    /** Shortcut to DynamicEnum.getMember */
    public static AssetType getMember(String name) throws NoSuchTypeException {
        try {
            return getMember(name, AssetType.class);
        } catch (NoSuchThingException e) {
            throw new NoSuchTypeException("No asset type: " + name, e);
        }
    }


    /**
     * Does littleware enforce name-uniqueness (on a given server cluster) 
     * for this asset-type ?
     * A false result indicates that two assets
     * of this type may have the same name.
     * This base class method returns:
     *         (getSuperType().isEmpty()) ? false : getSuperType ().isNameUnique ()
     *
     * @return true if each asset of this type has a unique name within the
     *          set of assets of this type within a single littleware asset repository.
     */
    public boolean isNameUnique() {
        return (getSuperType().isEmpty()) ? false : getSuperType().get().isNameUnique();
    }

    /**
     * Allow an asset-type to have one super type.
     * Methods that search based on AssetType, like AssetSearchManager.getByName,
     * will return objects assets matching the given type and
     * any of its subtypes.
     *
     * @return super-type, or null if no super-type (this implementation returns null)
     */
    public Maybe<AssetType> getSuperType() {
        return Maybe.empty();
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
        for ( Maybe<AssetType> maybe = Maybe.something( (AssetType) this);
                maybe.isSet();
                maybe = maybe.get().getSuperType()) {
            if (maybe.get().equals(atype_other)) {
                return true;
            }
        }
        return false;
    }

  

    /**
     * Whether a user must be in the ADMIN group to create this type of asset.
     * This base class method returns:
     *         (null == getSuperType()) ? false : getSuperType ().isAdminToCreate ()
     */
    public boolean isAdminToCreate() {
        return (getSuperType().isEmpty()) ? false : getSuperType().get().isAdminToCreate();
    }
    /** GENERIC asset-type */
    public static final AssetType.Specialized<AssetBuilder> GENERIC = new AssetType.Specialized<AssetBuilder>(UUIDFactory.parseUUID("E18D1B19D9714F6F8F49CF9B431EBF23"),
            "littleware.GENERIC") {};


    /** HOME asset-type - must be admin to create */
    public static final AssetType.Specialized<AssetBuilder> HOME = new AssetType.Specialized<AssetBuilder>(UUIDFactory.parseUUID("C06CC38C6BD24D48AB5E2D228612C179"),
            "littleware.HOME") {


        /** Always return true */
        @Override
        public boolean isAdminToCreate() {
            return true;
        }

        /** Always return true */
        @Override
        public boolean isNameUnique() {
            return true;
        }

        @Override
        public AssetBuilder create() {
            return new SimpleAssetBuilder( AssetType.HOME ) {
                @Override
                public UUID getHomeId() {
                    return getId();
                }
                @Override
                public UUID getFromId() {
                    return null;
                }
            };
        }
    };

    
    /** LINK assset-type */
    public static final AssetType.Specialized<AssetBuilder> LINK = new AssetType.Specialized<AssetBuilder>(UUIDFactory.parseUUID("926D122F82FE4F28A8F5C790E6733665"),
            "littleware.LINK") {};


    /**
     * Globally name-unique asset-type for setting up
     * distributed exclusion locks.
     * Can setup cron-job to delete old locks out of the repository.
     */
    public static final AssetType.Specialized<AssetBuilder> LOCK =
        new AssetType.Specialized<AssetBuilder>( UUIDFactory.parseUUID("5C52B28DA10A435B957AD5EF454F01C7"),
                    "littleware.LOCK" ) {

        @Override
        public boolean isNameUnique() { return true; }
    };

    /**
     * UNKNOWN asset-type - place holder for asset-types that we don't have a handler for.
     * The engine refuses to save/create/update assets with UNKNOWN type.
     */
    public static final AssetType.Specialized<AssetBuilder> UNKNOWN = new AssetType.Specialized<AssetBuilder>(UUIDFactory.parseUUID("EDC97D5F816044E69BFC289F4715BA45"),
            "littleware.UNKNOWN") {};

}

