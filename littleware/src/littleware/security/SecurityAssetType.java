/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import java.util.*;
import java.security.*;
import javax.security.auth.Subject;

import littleware.asset.*;

import littleware.base.UUIDFactory;
import littleware.base.FactoryException;
import littleware.base.Maybe;
import littleware.security.auth.SimpleSession;
import littleware.security.auth.LittleSession;

/** 
 * AssetType specializer and bucket for littleware.security
 * based AssetTypes.
 */
public abstract class SecurityAssetType extends AssetType {

    private static final long serialVersionUID = 4444442L;

    protected SecurityAssetType(UUID id, String name) {
        super(id, name);
    }
    /** 
     * PRINCIPAL asset type - with AccountManager asset specializer 
     * This asset-type is abstract - just intended for grouping
     * USER and GROUP types together.
     */
    public static final SecurityAssetType PRINCIPAL = new SecurityAssetType(
            UUIDFactory.parseUUID("A7E11221-5469-49FA-AF1E-8FCC52190F1D"),
            "littleware.PRINCIPAL") {

        /** Alwasy throws FactoryException */
        @Override
        public AssetBuilder create() {
            throw new FactoryException("PRINCIPAL asset-type is abstract");
        }

        /** USER and GROUP assets share the same namespace */
        @Override
        public boolean isNameUnique() {
            return true;
        }
    };

    public static class UserType extends SecurityAssetType {

        private UserType() {
            super(UUIDFactory.parseUUID("2FAFD5D1074F4BF8A4F01753DBFF4CD5"),
                    "littleware.USER");
        }

        @Override
        public boolean isAdminToCreate() {
            return true;
        }

        @Override
        public Maybe<AssetType> getSuperType() {
            return Maybe.something((AssetType) PRINCIPAL);
        }


        @Override
        public LittleUser.Builder create() {
            return new SimpleUserBuilder();
        }
    }

    /** USER asset type - with AccountManager asset specializer */
    public static final AssetType USER = new UserType();

    
    /** GROUP asset type - with AccountManager asset specializer */
    public static final AssetType GROUP = new AssetType(
            UUIDFactory.parseUUID("FAA894CEC15B49CF8F8EC5C280062776"),
            "littleware.GROUP") {

        /** Return a LittleGroup implementation */
        @Override
        public LittleGroup.Builder create() throws FactoryException {
            return new GroupBuilder();
        }

        @Override
        public Maybe<AssetType> getSuperType() {
            return Maybe.something((AssetType) PRINCIPAL);
        }
    };
    /** GROUP asset type - with AccountManager asset specializer */
    public static final AssetType GROUP_MEMBER = new AssetType(
            UUIDFactory.parseUUID("BA50260718204D50BAC6AC711CEE1536"),
            "littleware.GROUP_MEMBER") {

        @Override
        public Asset create() throws FactoryException {
            Asset a_result = new SimpleAssetBuilder();
            a_result.setAssetType(this);
            return a_result;
        }
    };
    /** ACL asset type - with AclManager asset specializer */
    public static final AssetType<LittleAcl> ACL = new AssetType<LittleAcl>(
            UUIDFactory.parseUUID("04E11B112526462F91152DFFB51D21C9"),
            "littleware.ACL") {

        /** Return a LittleAcl implementation */
        @Override
        public LittleAcl create() throws FactoryException {
            return new SimpleACLBuilder();
        }

        @Override
        public boolean isNameUnique() {
            return true;
        }
    };
    /** ACL_ENTRY asset type  */
    public static final AssetType<LittleAclEntry> ACL_ENTRY = new AssetType<LittleAclEntry>(
            UUIDFactory.parseUUID("D23EA8B5A55F4283AEF29DFA50C12C54"),
            "littleware.ACL_ENTRY") {

        @Override
        public LittleAclEntry create() throws FactoryException {
            return new AclEntryBuilder();
        }
    };
    /** SESSION asset type */
    public static final AssetType<LittleSession> SESSION = new AssetType<LittleSession>(
            UUIDFactory.parseUUID("7AC8C92F30C14AD89FA82DB0060E70C2"),
            "littleware.SESSION") {

        @Override
        public LittleSession create() throws FactoryException {
            LittleSession a_result = new SimpleSession();

            return a_result;
        }
    };
    /** 
     * SERVICE_STUB asset type - with NULL asset specializer 
     * - must be ADMIN to create -regulates access to SessionManager services.
     */
    public static final AssetType<Asset> SERVICE_STUB = new AssetType<Asset>(
            UUIDFactory.parseUUID("6AD504ACBB3A4A2CAB5AECE02D8E6706"),
            "littleware.SERVICE_STUB") {

        @Override
        public Asset create() throws FactoryException {
            Asset a_result = new SimpleAssetBuilder();
            a_result.setAssetType(this);
            return a_result;
        }

        @Override
        public boolean mustBeAdminToCreate() {
            return true;
        }
    };
    /** QUOTA asset type - with AccountManager asset specializer, must be admin group to create */
    public static final AssetType<Quota> QUOTA = new AssetType<Quota>(
            UUIDFactory.parseUUID("0897E6CF8A4C4B128ECABD92FEF793AF"),
            "littleware.QUOTA") {

        /** Return a LittleGroup implementation */
        @Override
        public Quota create() throws FactoryException {
            return new QuotaBuilder();
        }

        @Override
        public boolean mustBeAdminToCreate() {
            return true;
        }
    };

    /**
     * Extract the LittleUser from the Subject Principal set,
     * or return null if no LittleUser present
     */
    public static LittleUser getAuthenticatedUserOrNull(Subject j_user) {
        if (null == j_user) {
            return null;
        }
        Set<LittleUser> v_user = j_user.getPrincipals(LittleUser.class);
        if (v_user.isEmpty()) {
            return null;
        }
        return v_user.iterator().next();
    }

    /**
     * Shortcut to getAuthenticatedUserOrNull ( Subject.getSubject () )
     */
    public static LittleUser getAuthenticatedUserOrNull() {
        return getAuthenticatedUserOrNull(Subject.getSubject(AccessController.getContext()));
    }
}

