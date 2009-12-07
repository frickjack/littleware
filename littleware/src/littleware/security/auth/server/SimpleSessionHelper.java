/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server;

import java.rmi.RemoteException;
import java.util.UUID;
import java.security.*;
import javax.security.auth.*;

import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.client.LittleService;
import littleware.security.auth.*;
import littleware.security.AccessDeniedException;
import littleware.security.SecurityAssetType;
import littleware.security.LittleUser;
import littleware.base.*;

/**
 * Straight forward implementation of SessionHelper - 
 * deploys RMI-enabled managers wrapping timeout/read-only
 * aware proxies of standard Manager implementations.
 */
public class SimpleSessionHelper implements SessionHelper {

    private final UUID ou_session;
    private final AssetSearchManager om_search;
    private final AssetManager om_asset;
    private final SessionManager om_session;
    private final ServiceProviderRegistry oreg_service;

    public SimpleSessionHelper(UUID u_session,
            AssetSearchManager m_search,
            AssetManager m_asset,
            SessionManager m_session,
            ServiceProviderRegistry reg_service) {
        ou_session = u_session;
        om_search = m_search;
        om_asset = m_asset;
        om_session = m_session;
        oreg_service = reg_service;
    }

    @Override
    public LittleSession getSession() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        LittleSession a_session = (LittleSession) om_search.getAsset(ou_session).getOr(null);
        if (null == a_session) {
            throw new SessionExpiredException(ou_session.toString());
        }
        return a_session;
    }

    @Override
    public <T extends LittleService> T getService(ServiceType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return oreg_service.getService(n_type, this);
    }

    @Override
    public SessionHelper createNewSession(String s_session_comment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        try {
            LittleSession.Builder sessionBuilder = SecurityAssetType.SESSION.create();
            LittleUser p_caller = Subject.getSubject(AccessController.getContext()).
                    getPrincipals(LittleUser.class).iterator().next();

            sessionBuilder.setName(p_caller.getName() + ", " + sessionBuilder.getStartDate().getTime());
            sessionBuilder.setComment(s_session_comment);

            for (int i = 0; i < 20; ++i) {
                try {
                    final LittleSession session = om_asset.saveAsset(sessionBuilder.build(), s_session_comment).narrow();
                    i = 1000;
                    return om_session.getSessionHelper(session.getId());
                } catch (AlreadyExistsException e) {
                    if (i < 10) {
                        sessionBuilder.setName(p_caller.getName() + ", " + sessionBuilder.getStartDate().getTime() + "," + i);
                    } else {
                        throw new AccessDeniedException("Too many simultaneous session setups running for user: " + sessionBuilder.getName());
                    }
                }
            }
            throw new AssertionFailedException("Failed to derive an unused session name");
        } catch (FactoryException e) {
            throw new AssertionFailedException("Caught: " + e, e);
        } catch (NoSuchThingException e) {
            throw new AssertionFailedException("Caught: " + e, e);
        }
    }
}

