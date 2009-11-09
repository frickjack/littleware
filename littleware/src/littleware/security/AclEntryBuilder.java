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

import com.google.common.collect.ImmutableSet;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;
import java.security.acl.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.StringReader;

import littleware.asset.*;
import littleware.asset.xml.XmlDataAsset;
import littleware.base.*;

/**
 * Simple implementation of the LittleAclEntry interface.
 * Overrides setData/getData to extract XML permission data.
 */
class AclEntryBuilder extends SimpleAssetBuilder implements LittleAclEntry.Builder {

    private static final Logger log = Logger.getLogger("littleware.security.SimpleAclEntry");


    private Set<Permission> permissionSet = new HashSet<Permission>();
    private LittlePrincipal principal = null;

    @Override
    public LittleAclEntry build() {
        return new Entry( this, principal, permissionSet );
    }

    private static class Entry extends SimpleAssetBuilder.SimpleAsset implements LittleAclEntry {

        private static final long serialVersionUID = -5342316532664742997L;
        private LittlePrincipal principal;
        private Set<Permission> permissionSet;

        /** For serialization */
        private Entry() {}

        public Entry(AssetBuilder builder, LittlePrincipal principal,
                Set<Permission> permissionSet) {
            super(builder);
            this.principal = principal;
            this.permissionSet = ImmutableSet.copyOf(permissionSet);
        }

        @Override
        public LittlePrincipal getPrincipal() {
            if (null == principal) {
                throw new IllegalStateException("Principal not set");
            }
            return principal;
        }

        @Override
        public LittleAclEntry clone() {
            return new AclEntryBuilder().copy(this).build().narrow();
        }

        @Override
        public boolean setPrincipal(Principal user) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setNegativePermissions() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isNegative() {
            return (getValue() == 0);
        }

        @Override
        public boolean addPermission(Permission permission) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removePermission(Permission permission) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        /**
         * Check if the specified permission is part of this entry
         *
         * @param x_permission to check
         * @return true if in entry, false otherwise
         */
        @Override
        public boolean checkPermission(Permission x_permission) {
            return permissionSet.contains(x_permission);
        }

        @Override
        public Enumeration<Permission> permissions() {
            return Collections.enumeration(permissionSet);
        }

        @Override
        public LittleAclEntry.Builder copy() {
            return new AclEntryBuilder().copy( this );
        }

        /**
         * Get a simple String representation of this entry
         */
        @Override
        public String toString() {
            return "SimpleAclEntry( Principal: " + principal + ", permissions: " + permissionSet + ")";
        }

    }

    /** Do nothing default constructor */
    public AclEntryBuilder() {
        super(SecurityAssetType.ACL_ENTRY);
        setValue(1);
    }

    /**
     * Procedurally generate the data-string based on the permissions associated
     * with this entry
     */
    @Override
    public String getData() {
        final StringBuilder sb = new StringBuilder();
        sb.append("<acl:permlist xmlns:acl=\"http://www.littleware.com/xml/namespace/2006/acl\">\n");
        for (Permission permission : permissionSet) {
            sb.append("<acl:perm>");
            sb.append(UUIDFactory.makeCleanString(((LittlePermission) permission).getObjectId()));
            sb.append("</acl:perm>\n");
        }
        sb.append("</acl:permlist>\n");
        return sb.toString();
    }

    /**
     * SAX parser handler
     */
    private static class XmlDataHandler extends DefaultHandler {

        private String os_data = "";
        private boolean ob_getdata = false;
        private Set<Permission> ov_parse_perms = new HashSet<Permission>();

        /**
         * Callback for XML start-tag
         */
        @Override
        public void startElement(String s_namespace,
                String s_simple, // simple name (localName)
                String s_qualified, // qualified name
                Attributes v_attrs)
                throws SAXException {
            // Clear the data
			/*..
            olog_generic.log ( Level.FINE, "Starting element: " + s_simple +
            ", " + s_qualified
            );
            ..*/
            if (s_simple.equals("perm")) {
                os_data = "";
                ob_getdata = true;
            }
        }

        /**
         * Callback for XML end-element
         *
         * @param s_simple name of element
         * @param s_qualified name of element
         */
        @Override
        public void endElement(String s_namespace,
                String s_simple,
                String s_qualified)
                throws SAXException {
            if (ob_getdata) {
                String s_uuid = os_data.trim();

                ob_getdata = false;

                try {
                    UUID u_perm = UUIDFactory.parseUUID(s_uuid);
                    ov_parse_perms.add(LittlePermission.getMember(u_perm));
                } catch (IllegalArgumentException e) {
                    throw new SAXException("Invalid UUID: " + s_uuid, e);
                } catch (NoSuchThingException e) {
                    throw new SAXException("Invalid UUID: " + s_uuid, e);
                }
            }
        }

        @Override
        public void characters(char buf[], int offset, int len)
                throws SAXException {
            if (ob_getdata) {
                String s_in = new String(buf, offset, len);
                os_data += s_in;
            }
        }

        /**
         * Once parsing is complete - the set of permissions associated with this entry
         *   should be ready to go
         */
        public Set<Permission> getPermissions() {
            return ov_parse_perms;
        }

    }

    /**
     * Assign values to this entry's permission set based
     * on the supplied data
     *
     * @param ParseException if data not formatted correctly
     */
    @Override
    public void setData(String data) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser sax_parser = factory.newSAXParser();
            XmlDataHandler sax_handler = new XmlDataHandler();

            sax_parser.parse(new InputSource(new StringReader(data)),
                    sax_handler);
            permissionSet = sax_handler.getPermissions();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to parse: " + e, e);
        }
    }

    @Override
    public LittleAclEntry.Builder copy( Asset other ) {
        super.copy( other );
        return principal( ((LittleAclEntry) other).getPrincipal() );
    }

    /**
     * Add the specified permission to this Acl entry
     *
     * @param x_permission to add
     * @return true if permission added, false if already in entry
     */
    @Override
    public LittleAclEntry.Builder addPermission(Permission permission) {
        permissionSet.add(permission);
        return this;
    }

    /**
     * Set this Acl Entry as a negative one - shortcut to setValue(0)
     */
    @Override
    public void setNegative() {
        setValue(0);
    }

    @Override
    public LittleAclEntry.Builder negative() {
        setNegative();
        return this;
    }

    /**
     * Set the principal this entry tracks permissions for.
     * Also resets setToId to the principal id,
     * and sets name to principal-name.
     *
     * @param p_principal to track
     */
    @Override
    public void setPrincipal(LittlePrincipal principal) {
        this.principal = (LittlePrincipal) principal;
        setToId(principal.getId());
    }
    @Override
    public LittleAclEntry.Builder principal( LittlePrincipal principal ) {
        setPrincipal( principal );
        return this;
    }
}

