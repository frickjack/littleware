/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.internal;

import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.validate.ValidationException;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import java.util.logging.Logger;


import littleware.security.LittleAclEntry.Builder;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import java.io.StringReader;

import java.security.acl.Permission;
import littleware.base.*;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittlePermission;
import littleware.security.LittlePrincipal;

/**
 * Simple implementation of the LittleAclEntry interface.
 * Overrides setData/getData to extract XML permission data.f
 */
class AclEntryBuilder extends AbstractAssetBuilder<LittleAclEntry.Builder> implements LittleAclEntry.Builder {
    private static final Logger log = Logger.getLogger( AclEntryBuilder.class.getName() );


    private Set<Permission> permissionSet = new HashSet<Permission>();
    private LittlePrincipal principal = null;

    @Override
    public LittleAclEntry build() {
        return new EntryAsset( this, principal, permissionSet );
    }


    @Override
    public Builder acl(LittleAcl acl) {
        super.parent( acl );
        ownerId( acl.getOwnerId() );
        return aclId( acl.getId() );
    }

    @Override
    public Builder aclId( UUID id ) {
        super.aclId( id );
        return fromId( id );
    }


    //------------------------------------------------------

    private static class EntryAsset extends AbstractAsset implements LittleAclEntry {

        private static final long serialVersionUID = -5342316532664742997L;
        private LittlePrincipal principal;
        private Set<Permission> permissionSet;

        /** For serialization */
        private EntryAsset() {}

        public EntryAsset(AclEntryBuilder builder, LittlePrincipal principal,
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
        public boolean isNegative() {
            return (getValue() == 0);
        }


        /**
         * Check if the specified permission is part of this entry
         *
         * @param x_permission to check
         * @return true if in entry, false otherwise
         */
        @Override
        public boolean checkPermission(Permission permission) {
            return permissionSet.contains(permission);
        }

        @Override
        public Enumeration<Permission> permissions() {
            return Collections.enumeration(permissionSet);
        }
        @Override
        public Collection<Permission> getPermissions() {
            return permissionSet;
        }

        @Override
        public LittleAclEntry.Builder copy() {
            return (new AclEntryBuilder()).copy( this );
        }

        /**
         * Get a simple String representation of this entry
         */
        @Override
        public String toString() {
            return "SimpleAclEntry( Principal: " + principal + ", permissions: " + permissionSet + ")";
        }

        @Override
        public boolean equals( Object other ) {
            if ( (null == other)
                    || ! (other instanceof LittleAclEntry) ) {
                return false;
            }
            final LittleAclEntry entry = (LittleAclEntry) other;
            return super.equals( entry )
                || ( Whatever.get().equalsSafe(entry.getOwningAclId(), getFromId() )
                    && Whatever.get().equalsSafe( entry.getPrincipalId(), getToId() )
                    && entry.isNegative() == isNegative()
                    );
        }



        @Override
        public int hashCode() {
            int hash = 7 + super.hashCode();
            hash = 47 * hash + (this.getFromId() != null ? this.getFromId().hashCode() : 0);
            hash = 47 * hash + (this.getToId() != null ? this.getToId().hashCode() : 0);
            hash = 47 * hash + (isNegative() ? 1 : 0);
            return hash;
        }

        @Override
        public UUID getOwningAclId() {
            return getFromId();
        }

        @Override
        public UUID getPrincipalId() {
            return getToId();
        }
    }

    //--------------------------------------------------

    /** Do nothing default constructor */
    public AclEntryBuilder() {
        super(LittleAclEntry.ACL_ENTRY);
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

    //-----------------------------------------------------

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

    //-----------------------------------------------------

    /**
     * Assign values to this entry's permission set based
     * on the supplied data
     */
    @Override
    public LittleAclEntry.Builder data(String data) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            SAXParser sax_parser = factory.newSAXParser();
            XmlDataHandler sax_handler = new XmlDataHandler();

            sax_parser.parse(new InputSource(new StringReader(data)),
                    sax_handler);
            permissionSet = sax_handler.getPermissions();
            return this;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidationException("Failed to parse: " + e, e);
        }
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
    @Override
    public LittleAclEntry.Builder removePermission( Permission permission ) {
        permissionSet.remove(permission);
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
     */
    @Override
    public void setPrincipal(LittlePrincipal principal) {
        this.principal = principal;
        if ( null != principal ) {
            // principal may not be set
            setToId(principal.getId());
        }
    }
    
    @Override
    public LittleAclEntry.Builder principal( LittlePrincipal principal ) {
        setPrincipal( principal );
        return this;
    }
}

