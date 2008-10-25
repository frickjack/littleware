package littleware.security;

import java.util.*;
import java.security.*;
import javax.security.auth.Subject;

import littleware.asset.*;

import littleware.base.UUIDFactory;
import littleware.base.FactoryException;
import littleware.base.PropertiesLoader;
import littleware.security.auth.SimpleSession;
import littleware.security.auth.LittleSession;

/** 
 * AssetType specializer and bucket for littleware.security
 * based AssetTypes.
 */
public abstract class SecurityAssetType<T extends Asset> extends AssetType<T> {
    private static final long serialVersionUID = 4444442L;
    private static AssetSpecializer om_account = null;
    private static AssetSpecializer om_acl = null;

    private static AssetSpecializer getAccountManager() {
        if (null == om_account) {
            ResourceBundle bundle_security = PropertiesLoader.get().getBundle("littleware.security.server.SecurityResourceBundle");
            om_account = (AssetSpecializer) bundle_security.getObject("AccountManager");
        }
        return om_account;
    }

    private static AssetSpecializer getAclManager() {
        if (null == om_acl) {
            ResourceBundle bundle_security = PropertiesLoader.get().getBundle("littleware.security.server.SecurityResourceBundle");
            om_acl = (AssetSpecializer) bundle_security.getObject("AclManager");
        }
        return om_acl;
    }
    /** 
     * PRINCIPAL asset type - with AccountManager asset specializer 
     * This asset-type is abstract - just intended for grouping
     * USER and GROUP types together.
     */
    public static final AssetType<LittlePrincipal> PRINCIPAL = new AssetType<LittlePrincipal>(
            UUIDFactory.parseUUID("A7E11221-5469-49FA-AF1E-8FCC52190F1D"),
            "littleware.PRINCIPAL") {

        @Override
        public AssetSpecializer getSpecializer() {
            return getAccountManager();
        }

        /** Alwasy throws FactoryException */
        public LittlePrincipal create() throws FactoryException {
            throw new FactoryException("PRINCIPAL asset-type is abstract");
        }

        /** USER and GROUP assets share the same namespace */
        @Override
        public boolean isNameUnique() {
            return true;
        }
    };
    /** USER asset type - with AccountManager asset specializer */
    public static final AssetType<LittleUser> USER = new AssetType<LittleUser>(
            UUIDFactory.parseUUID("2FAFD5D1074F4BF8A4F01753DBFF4CD5"),
            "littleware.USER") {

        @Override
        public AssetSpecializer getSpecializer() {
            return getAccountManager();
        }

        /** Get a LittleUser implementation */
        public LittleUser create() throws FactoryException {
            return new SimpleUser();
        }

        @Override
        public boolean mustBeAdminToCreate() {
            return true;
        }

        @Override
        public AssetType getSuperType() {
            return PRINCIPAL;
        }
    };
    /** GROUP asset type - with AccountManager asset specializer */
    public static final AssetType<LittleGroup> GROUP = new AssetType<LittleGroup>(
            UUIDFactory.parseUUID("FAA894CEC15B49CF8F8EC5C280062776"),
            "littleware.GROUP") {

        @Override
        public AssetSpecializer getSpecializer() {
            return getAccountManager();
        }

        /** Return a LittleGroup implementation */
        public LittleGroup create() throws FactoryException {
            return new SimpleGroup();
        }

        @Override
        public AssetType getSuperType() {
            return PRINCIPAL;
        }
    };
    /** GROUP asset type - with AccountManager asset specializer */
    public static final AssetType<Asset> GROUP_MEMBER = new AssetType<Asset>(
            UUIDFactory.parseUUID("BA50260718204D50BAC6AC711CEE1536"),
            "littleware.GROUP_MEMBER") {

        public Asset create() throws FactoryException {
            Asset a_result = new SimpleAsset();
            a_result.setAssetType(this);
            return a_result;
        }
    };
    /** ACL asset type - with AclManager asset specializer */
    public static final AssetType<LittleAcl> ACL = new AssetType<LittleAcl>(
            UUIDFactory.parseUUID("04E11B112526462F91152DFFB51D21C9"),
            "littleware.ACL") {

        /** Return a LittleAcl implementation */
        public LittleAcl create() throws FactoryException {
            return new SimpleAccessList();
        }

        @Override
        public AssetSpecializer getSpecializer() {
            return getAclManager();
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

        public LittleAclEntry create() throws FactoryException {
            return new SimpleAclEntry();
        }
    };
    /** SESSION asset type */
    public static final AssetType<LittleSession> SESSION = new AssetType<LittleSession>(
            UUIDFactory.parseUUID("7AC8C92F30C14AD89FA82DB0060E70C2"),
            "littleware.SESSION") {

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

        public Asset create() throws FactoryException {
            Asset a_result = new SimpleAsset();
            a_result.setAssetType(this);
            return a_result;
        }

        public boolean mustBeAdminToCreate() {
            return true;
        }
    };
    /** QUOTA asset type - with AccountManager asset specializer, must be admin group to create */
    public static final AssetType<Quota> QUOTA = new AssetType<Quota>(
            UUIDFactory.parseUUID("0897E6CF8A4C4B128ECABD92FEF793AF"),
            "littleware.QUOTA") {

        public AssetSpecializer getSpecializer() {
            return getAccountManager();
        }

        /** Return a LittleGroup implementation */
        public Quota create() throws FactoryException {
            return new SimpleQuota();
        }

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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

