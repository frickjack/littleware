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


package littleware.apps.test;

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.mail.internet.*;
import java.net.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.xml.*;
import littleware.apps.addressbook.*;
import littleware.base.*;
import littleware.security.*;
import littleware.security.auth.LittleSession;

/**
 * Tester for the SimpleXmlDataAsset Asset super class handling
 * of XML-based getData/setData via annotations.
 */
public class AddressBookTester extends TestCase {

    private static final String os_test_name = "Frickjack";
    private static final Logger olog_generic = Logger.getLogger(AddressBookTester.class.getName() );
    private final LittleSession  osession;
    private final AssetManager   om_asset;
    private final AssetSearchManager om_search;

    /**
     * Inject dependencies.
     */
    public AddressBookTester(String s_test_name,
            AssetManager m_asset,
            AssetSearchManager m_search,
            LittleSession session
            ) 
    {
        super(s_test_name);
        om_asset = m_asset;
        om_search = m_search;
        osession = session;

    }
    
    /**
     * Inject dependencies.
     */
    @Inject
    public AddressBookTester(
            AssetManager m_asset,
            AssetSearchManager m_search,
            LittleSession session
            ) 
    {
        this( "", m_asset, m_search, session );
    }

    /**
     * Make sure the test_user contact was torn down by the last test.
     */
    public void setUp() {
        tearDown();
    }

    /** 
     * Delete the test_user contact
     */
    @Override
    public void tearDown() {
        try {
            UUID u_contact = om_search.getAssetIdsFrom(osession.getOwnerId(),
                    AddressAssetType.CONTACT).get(os_test_name);
            if (null != u_contact) {
                Contact contact_old = (Contact) om_search.getAsset(u_contact);
                List<littleware.apps.addressbook.Address> v_addr = contact_old.getAddress();

                for (littleware.apps.addressbook.Address addr_old : v_addr) {
                    olog_generic.log(Level.INFO, "... deleting address: " + addr_old.getName() +
                            " (" + addr_old.getObjectId() + ")");
                    contact_old.removeAddress(addr_old);
                    om_asset.deleteAsset(addr_old.getObjectId(), "test cleanup");
                }
                om_asset.deleteAsset(contact_old.getObjectId(), "test_cleanup");
            }
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Failed tearDown, caught: " + e + ", " +
                    BaseException.getStackTrace(e));
            throw new AssertionFailedException("Failed setup, caught: " + e, e);
        }
    }

    /**
     * Run the Contact and Address asset types through a create, update, lookup,
     * delete cycle. 
     */
    public void testAddressBook() {
        try {
            Contact contact_test = AddressAssetType.CONTACT.create();
            contact_test.setFromId(osession.getOwnerId () );
            contact_test.setHomeId(osession.getHomeId());
            contact_test.setName(os_test_name);
            contact_test.setFirstName("Frick");
            contact_test.setMiddleName("Jack");
            {
                Address addr_home = AddressAssetType.ADDRESS.create();
                addr_home.setName(AddressType.HOME.toString());
                addr_home.setAddressType(AddressType.HOME);
                addr_home.setEmail(new InternetAddress("frickjack@yahoo.com"));
                addr_home.setPhone("555-555-5555");
                contact_test.addAddress(addr_home, 0);

                if (addr_home instanceof XmlDataAsset) {
                    String s_xml = addr_home.getData();

                    olog_generic.log(Level.INFO, "Address home data: " + s_xml);
                    addr_home.setData(s_xml);
                    assertTrue("Address home getData/setData consistency",
                            addr_home.getData().equals(s_xml));
                }
            }
            {
                Address addr_work = AddressAssetType.ADDRESS.create();
                addr_work.setName(AddressType.BUSINESS.toString());
                addr_work.setAddressType(AddressType.BUSINESS);
                addr_work.setEmail(new InternetAddress("jackfrick@work.com"));
                addr_work.setUrl(new URL("http://www.work.com/~frickjack/"));
                contact_test.addAddress(addr_work, -1);
                if (addr_work instanceof XmlDataAsset) {
                    String s_xml = addr_work.getData();

                    olog_generic.log(Level.INFO, "Address work data: " + s_xml);
                    addr_work.setData(s_xml);
                    assertTrue("Address work getData/setData consistency",
                            addr_work.getData().equals(s_xml));
                }
            }
            contact_test = (Contact) om_asset.saveAsset(contact_test, "new contact test");

            olog_generic.log(Level.INFO, "Trying to reload Contact: " + contact_test.getObjectId());
            Contact contact_load = (Contact) om_search.getAsset(contact_test.getObjectId());
            assertTrue("Middle names match on reload: " + contact_test.getMiddleName() +
                    " =? " + contact_load.getMiddleName(),
                    contact_load.getMiddleName().equals(contact_test.getMiddleName()));
            assertTrue("Last names match on reload: " + contact_test.getLastName() +
                    " =? " + contact_load.getLastName(),
                    contact_load.getLastName().equals(contact_test.getLastName()));
            if (contact_load instanceof XmlDataAsset) {
                String s_xml = contact_load.getData();

                olog_generic.log(Level.INFO, "Contact info: " + s_xml);
                contact_load.setData(s_xml);
                assertTrue("Contact getData/setData consistency",
                        contact_load.getData().equals(s_xml));
            }

            assertTrue("Contact reload loaded addresses",
                    contact_load.getAddress().size() == contact_test.getAddress().size());

            assertTrue("Contact reload preserved address order",
                    contact_load.getFirstAddress().equals(contact_test.getFirstAddress()));
            {
                /** Check data update */
                Address addr_test = contact_load.getFirstAddress();
                assertTrue("Loaded address is an XmlDataAsset",
                        addr_test instanceof XmlDataAsset);
                String s_data1 = addr_test.getData();
                addr_test.setEmail(new InternetAddress("reload@work.com"));
                String s_data2 = addr_test.getData();
                assertTrue("E-mail update reflected in data: " + s_data2,
                        !s_data1.equals(s_data2));
                olog_generic.log(Level.INFO,
                        "Email update got valid data: " +
                        addr_test.getData());
                addr_test.setData(addr_test.getData());
                assertTrue("setData preserves e-mail address",
                        addr_test.getEmail().equals(new InternetAddress("reload@work.com")));
            }

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}

