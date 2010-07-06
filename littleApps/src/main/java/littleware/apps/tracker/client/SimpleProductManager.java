/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.client;

import java.io.File;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberIndex;
import littleware.apps.tracker.ProductManager;
import littleware.base.BaseException;

public class SimpleProductManager implements ProductManager {

    @Override
    public void checkout(UUID memberId, File destinationFolder) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Member checkin(UUID versionId, String memberName, File source, String comment) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MemberIndex getIndex(UUID memberId) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
