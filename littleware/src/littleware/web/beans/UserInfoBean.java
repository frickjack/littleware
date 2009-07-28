/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.beans;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import littleware.apps.addressbook.Address;
import littleware.apps.addressbook.AddressAssetType;
import littleware.apps.addressbook.AddressType;
import littleware.apps.addressbook.Contact;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.security.LittleUser;
import littleware.security.auth.ServiceType;

/**
 *
 * @author rdp0004
 */
public class UserInfoBean {
    private final SessionBean session;

    @Inject
    public UserInfoBean( SessionBean session ) {
        this.session = session;
    }

    /**
     * Get the contact info for the user with the given id -
     * create and save stub info if Contact info does
     * not already exist for the given user.
     *
     * @param user to get contact info for
     * @return Contact associated with user
     * @throws java.rmi.RemoteException
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws littleware.asset.AssetException
     */
    public Contact lookupContact( LittleUser user ) throws RemoteException,
            BaseException, GeneralSecurityException, AssetException {
        AssetSearchManager m_search = null; //helper.getService(ServiceType.ASSET_SEARCH);
        AssetManager m_asset = null; //helper.getService(ServiceType.ASSET_MANAGER);
        UUID u_link = m_search.getAssetIdsFrom(user.getObjectId(),
                AssetType.LINK).get("contact");
        Asset a_link = null;
        AssetBuilder builder = new AssetBuilder ().setHomeId( user.getHomeId () )
                .setOwnerId( user.getObjectId () );

        if (null == u_link) {
            a_link = builder.setName( "contact" )
                    .setFromId( user.getObjectId () )
                    .create ( AssetType.LINK );
        } else {
            a_link = m_search.getAsset(u_link).get();
        }

        Contact contact_user = null;

        if (null != a_link.getToId()) {
            contact_user = (Contact) m_search.getAsset(a_link.getToId()).getOr(null);
        }
        if (null == contact_user) {
            contact_user = builder.setFromId( null )
                    .setName( user.getName () )
                    .create ( AddressAssetType.CONTACT );
                        contact_user.setLastName("unknown");
            contact_user.setFirstName("unknown");
            a_link.setToId(contact_user.getObjectId());
        }

        Address addr_first = contact_user.getFirstAddress();
        if (null == addr_first) {
            Address addr_bootstrap = builder.setName( "default" )
                    .create( AddressAssetType.ADDRESS );
            try {
                addr_bootstrap.setEmail(new InternetAddress("unknown@unknown.com"));
            } catch (AddressException e) {
                throw new AssertionFailedException ( "Failure setting bogus bootstrap e-mail address", e );
            }
            addr_bootstrap.setAddressType(AddressType.HOME);
            contact_user.addAddress(addr_bootstrap, 0);

            List<Asset> v_save = new ArrayList<Asset> ();
            v_save.add ( contact_user );
            v_save.add ( a_link );
            session.getAssetLib().syncAsset( m_asset.saveAssetsInOrder(v_save, "bootstrapping user contact" ) );
            // get the post-save asset
            contact_user = (Contact) session.getAssetLib ().retrieveAssetModel( contact_user.getObjectId (), m_search).get().getAsset ();
        }

        return contact_user;
    }

    /**
     * Get the contact-information attached to the
     * authenticated user.  Autogenerate info if not already present.
     *
     * @return Contact asset, or null if isGuest
     */
    public Contact getContact() throws RemoteException,
            BaseException, GeneralSecurityException, AssetException {
        if ( session.isGuest() ) {
            return null;
        }
        AssetSearchManager m_search = session.getHelper().getService(ServiceType.ASSET_SEARCH);

        return lookupContact( session.getHelper().getSession().getCreator(m_search) );
    }

}
