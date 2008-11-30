package littleware.apps.addressbook.server;

import com.google.inject.Inject;
import littleware.asset.server.AssetSpecializer;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.security.GeneralSecurityException;

import littleware.apps.addressbook.*;
import littleware.base.*;
import littleware.asset.*;

/** 
 * Handle specialization of a Contact type asset -
 * load the Address assets associated with the Contact.
 */
public class AddressSpecializer implements AssetSpecializer {

    private static final Logger olog_generic = Logger.getLogger("littleware.apps.addressbook.server.AddressSpecializer");
    private final AssetRetriever om_retriever;

    /**
     * Inject dependency on AssetRetriever
     */
    @Inject
    public AddressSpecializer(AssetRetriever m_retriever) {
        om_retriever = m_retriever;
    }

    public <T extends Asset> T narrow(T a_in, AssetRetriever m_retriever) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (!a_in.getAssetType().equals(AddressAssetType.CONTACT)) {
            return a_in;
        }

        Contact addr_contact = (Contact) a_in;
        Map<String, UUID> v_children = m_retriever.getAssetIdsFrom(addr_contact.getObjectId(),
                AddressAssetType.ADDRESS);
        Set<Asset> v_address = m_retriever.getAssets(v_children.values());
        List<Asset> v_sort = new ArrayList(v_address);
        Comparator<Asset> x_sort = new Comparator<Asset>() {

            public int compare(Asset a_1, Asset a_2) {
                return Float.compare(a_1.getValue().floatValue(), a_2.getValue().floatValue());
            }
        };

        Collections.sort(v_sort, x_sort);
        addr_contact.clearAddress();
        for (Asset a_address : v_sort) {
            addr_contact.addAddress((Address) a_address, -1);
        }
        return a_in;
    }

    public void postCreateCallback(Asset a_new, AssetManager m_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (!(a_new instanceof Contact)) {
            return;
        }

        // Save all the assets in the asset-list
        Contact addr_new = (Contact) a_new;
        int i = 0;
        for (Address addr_contact : addr_new.getAddress()) {
            addr_contact.setContactId(addr_new.getObjectId());
            addr_contact.setValue(i);
            m_asset.saveAsset(addr_contact, "positioning address in Contact address list");
            ++i;
        }
    }

    public void postUpdateCallback(Asset a_pre_update, Asset a_now, AssetManager m_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (!(a_now instanceof Contact)) {
            return;
        }
        Contact addr_now = (Contact) a_now;
        Contact addr_pre_update = (Contact) a_pre_update;

        postCreateCallback(addr_now, m_asset);

        // Make sure the ContactId is cleared for addresses removed from this Contact
        Set<Address> v_old = new HashSet(addr_pre_update.getAddress());
        v_old.removeAll(addr_now.getAddress());

        for (Address addr_old : v_old) {
            Address addr_load = (Address) om_retriever.getAssetOrNull(addr_old.getObjectId());

            if ((null != addr_load) && (null != addr_load.getObjectId()) && addr_now.getObjectId().equals(addr_load.getObjectId())) {
                addr_load.setFromId(null);
                m_asset.saveAsset(addr_load, "removing from Contact address list");
            }
        }
    }

    public void postDeleteCallback(Asset a_deleted, AssetManager m_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        // Clear out the contact-id of Contact Addresses
    }
}// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

