/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.internal;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.Member.MemberBuilder;
import littleware.apps.tracker.MemberIndex;
import littleware.apps.tracker.ProductManager;
import littleware.apps.tracker.Version;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.BaseException;

public class SimpleMemberBuilder extends AbstractAssetBuilder<MemberBuilder> implements MemberBuilder {

    public SimpleMemberBuilder() {
        super(Member.MemberType);
    }


    @Override
    public MemberBuilder version(Version value) {
        return parentInternal(value);
    }

    @Override
    public Member build() {
        return new SimpleMember(this);
    }

    public static class SimpleMember extends AbstractAsset implements Member {
        private transient ProductManager prodMan;

        private SimpleMember() {
        }

        private SimpleMember(SimpleMemberBuilder builder) {
            super(builder);
        }

        @Inject
        public void inijectMe( ProductManager prodMan ) {
            this.prodMan = prodMan;
        }

        @Override
        public UUID getVersionId() {
            return getFromId();
        }

        @Override
        public MemberBuilder copy() {
            return (new SimpleMemberBuilder()).copy(this);
        }

        @Override
        public float getSizeMB() {
            return getValue();
        }

        @Override
        public MemberIndex getIndex() throws BaseException, GeneralSecurityException, RemoteException {
            return prodMan.loadIndex(this.getId() );
        }
    }
}
