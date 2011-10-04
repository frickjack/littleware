/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.internal;

import littleware.base.AssertionFailedException;
import biz.source_code.base64Coder.Base64Coder;
import java.security.MessageDigest;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.asset.Asset;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.base.validate.ValidationException;
import littleware.security.LittleUser;
import littleware.security.LittleUser.Builder;
import static littleware.security.LittleUser.Status;

/**
 * Simple implementation of the SimpleUserBuilder interface
 */
public class SimpleUserBuilder extends AbstractAssetBuilder<LittleUser.Builder> implements LittleUser.Builder {

    private static final Logger log = Logger.getLogger(SimpleUserBuilder.class.getName());

    /** Do-nothing constructor for java.io.Serializable */
    public SimpleUserBuilder() {
        super(LittleUser.USER_TYPE);
        setOwnerId(getId());
    }

    @Override
    public Status getStatus() {
        if (getState() == Status.ACTIVE.ordinal()) {
            return Status.ACTIVE;
        }
        return Status.INACTIVE;
    }

    @Override
    public void setStatus(Status status) {
        setState(status.ordinal());
    }

    @Override
    public LittleUser.Builder status(Status status) {
        setStatus(status);
        return this;
    }

    @Override
    public LittleUser.Builder state(int state) {
        if ((state < 0) || (state > Status.values().length - 1)) {
            throw new IllegalArgumentException("Illegal user state: " + state);
        }
        super.state(state);
        return this;
    }

    @Override
    public LittleUser.Builder copy(Asset value) {
        return (Builder) super.copy(value);
    }

    @Override
    public final void setPassword(String value) {
        password( value );
    }

    @Override
    public Builder password(String value) {
        final String salt = UUIDFactory.makeCleanString(UUID.randomUUID());
        putAttribute( "salt", salt );
        putAttribute( "password", passwordHash( value, salt ) );
        return this;
    }

    private static class User extends AbstractAsset implements LittleUser {

        /** For serialization */
        private User() {
        }

        public User(SimpleUserBuilder builder) {
            super(builder);
        }

        @Override
        public Status getStatus() {
            final int state = getState();
            for (Status status : Status.values()) {
                if (state == status.ordinal()) {
                    return status;
                }
            }
            throw new ValidationException("Unknown state: " + getState());
        }

        @Override
        public LittleUser.Builder copy() {
            return (new SimpleUserBuilder()).copy(this);
        }

        @Override
        public String getHashedPassword() {
            return getAttribute("password").getOr("");
        }

        @Override
        public boolean testPassword(String guess) {
            return getHashedPassword().equals(applyPasswordHash(guess));
        }

        @Override
        public String applyPasswordHash(String value) {
            return passwordHash( value, getAttribute( "salt" ).getOr( "bla" ) );
        }
    }

    private static String passwordHash(String value, String salt ) {
        if ( Whatever.get().empty(value)) {
            return "";
        }
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.reset();
            digest.update( salt.getBytes( "UTF-8" ) );
            return new String(Base64Coder.encode(digest.digest(value.getBytes("UTF-8"))));
        } catch (Exception ex) {
            throw new AssertionFailedException("Cannot hash passwords", ex);
        }
    }

    @Override
    public LittleUser build() {
        return new User(this);
    }
}
