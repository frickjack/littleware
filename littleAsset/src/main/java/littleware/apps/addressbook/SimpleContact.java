/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.addressbook;

import java.util.*;

import littleware.asset.*;
import littleware.asset.xml.*;

/**
 * Simple implementation of Contact interface
 */
public class SimpleContact extends SimpleXmlAssetBuilder implements Contact {

    private static final long serialVersionUID = -6466853250780135276L;
    private String os_first_name = null;
    private String os_middle_name = null;
    private LinkedList<Address> ov_address = new LinkedList<Address>();
    public final static String OS_XML_NAMESPACE =
            "http://www.littleware.com/xml/namespace/2006/addressbook";
    public final static String OS_XML_PREFIX = "addr";
    public final static String OS_XML_ROOT = "contact";

    /**
     * Constructor registers asset-type and XML support-data
     * with super.
     */
    public SimpleContact() {
        super(OS_XML_NAMESPACE, OS_XML_PREFIX, OS_XML_ROOT);
        setAssetType(AddressAssetType.CONTACT);
    }

    @XmlGetter(element = "first")
    @Override
    public String getFirstName() {
        return os_first_name;
    }

    @XmlSetter(element = "first")
    @Override
    public void setFirstName(String s_first_name) {
        os_first_name = s_first_name;
    }

    /**
     * Data stashed in XML data
     */
    @XmlGetter(element = "middle")
    @Override
    public String getMiddleName() {
        return os_middle_name;
    }

    @XmlSetter(element = "middle")
    @Override
    public void setMiddleName(String s_middle_name) {
        os_middle_name = s_middle_name;
    }

    @Override
    public String getLastName() {
        return getName();
    }

    /**
     * Last-name doubles for getName() saved in database,
     * so do not need to pull out of XML data
     */
    @Override
    public void setLastName(String s_last_name) {
        setName(s_last_name);
    }

    @Override
    public synchronized List<Address> getAddress() {
        return new ArrayList<Address>(ov_address);
    }

    @Override
    public synchronized Address getFirstAddress(AddressType n_type) {
        for (Address addr_check : ov_address) {
            if (addr_check.getAddressType().equals(n_type)) {
                return addr_check;
            }
        }
        return null;
    }

    @Override
    public synchronized Address getFirstAddress() {
        if (ov_address.isEmpty()) {
            return null;
        }
        return ov_address.get(0);
    }

    @Override
    public synchronized void addAddress(Address addr_new, int i_position) {
        removeAddress(addr_new);
        if ((i_position < 0) || (i_position > ov_address.size())) {
            i_position = ov_address.size();
        }
        addr_new.setContactId(getObjectId());
        addr_new.setValue(i_position);
        addr_new.setHomeId(getHomeId());
        ov_address.add(i_position, addr_new);
    }

    @Override
    public synchronized void removeAddress(Address addr_old) {
        ov_address.remove(addr_old);
    }

    @Override
    public synchronized void clearAddress() {
        ov_address.clear();
    }

    /**
     * Return a simple copy of this object - except setup
     * an empty address list - since we assume that the
     * specializer will load that anyway.
     */
    @Override
    public SimpleContact clone() {
        SimpleContact contact_clone = (SimpleContact) super.clone();
        contact_clone.ov_address = (LinkedList<Address>) ov_address.clone();
        return contact_clone;
    }

    @Override
    public void sync(Asset a_copy_source) {
        if (this == a_copy_source) {
            return;
        }
        super.sync(a_copy_source);
        SimpleContact contact_copy_source = (SimpleContact) a_copy_source;
        os_first_name = contact_copy_source.os_first_name;
        os_middle_name = contact_copy_source.os_middle_name;
        ov_address = (LinkedList<Address>) contact_copy_source.ov_address.clone();
    }
}

