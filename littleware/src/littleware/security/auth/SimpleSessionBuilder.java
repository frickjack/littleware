/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth;

import java.rmi.RemoteException;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.security.Principal;
import java.security.GeneralSecurityException;
import javax.security.auth.*;

import littleware.security.*;
import littleware.base.*;
import littleware.asset.*;
import littleware.security.auth.LittleSession.Builder;

/**
 * Simple implementation of LittleSession 
 * interface backed by a database entry.
 */
public class SimpleSessionBuilder extends SimpleAssetBuilder implements LittleSession.Builder {

    /** Do-nothing constructor for java.io.Serializable */
    public SimpleSessionBuilder() {
        super(SecurityAssetType.SESSION);
        final Date now = new Date();
        this.setStartDate(now);
        this.setEndDate(new Date(now.getTime() + 60 * 60 * 24 * 1000));
    }

    private static class SessionAsset extends SimpleAsset implements LittleSession {

        /** For serialization */
        public SessionAsset() {
        }

        public SessionAsset(SimpleSessionBuilder builder) {
            super(builder);
        }

        /** Just return the name */
        @Override
        public String toString() {
            return getName();
        }

        @Override
        public boolean isReadOnly() {
            return (getValue() != 0);
        }
        private transient Subject subject;

        @Override
        public Subject getSubject(AssetRetriever m_retriever) throws BaseException, AssetException,
                GeneralSecurityException, RemoteException {
            if (null == subject) {
                final LittleUser a_user = m_retriever.getAsset(getOwnerId()).get().narrow();
                Set<Principal> v_principals = new HashSet<Principal>();
                v_principals.add(a_user);
                subject = new Subject(true, v_principals, new HashSet<Object>(), new HashSet<Object>());
            }
            return subject;
        }

        @Override
        public LittleSession.Builder copy() {
            return (Builder) super.copy();
        }
    }

    @Override
    public boolean isReadOnly() {
        return (getValue() != 0);
    }

    @Override
    public void setReadOnly(boolean value) {
        setValue(value ? 1 : 0);
    }

    @Override
    public LittleSession.Builder readOnly(boolean value) {
        setReadOnly(value);
        return this;
    }

    @Override
    public LittleSession.Builder copy(Asset source) {
        return (Builder) super.copy(source);
    }

    @Override
    public LittleSession build() {
        return new SessionAsset(this);
    }
}

