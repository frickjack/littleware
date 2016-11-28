package littleware.security.auth.internal;

import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import java.util.Date;

import littleware.asset.*;
import littleware.security.auth.LittleSession;
import littleware.security.auth.LittleSession.Builder;

/**
 * Simple implementation of LittleSession 
 * interface backed by a database entry.
 */
public class SimpleSessionBuilder extends AbstractAssetBuilder<LittleSession.Builder> implements LittleSession.Builder {

    /** Do-nothing constructor for java.io.Serializable */
    public SimpleSessionBuilder() {
        super(LittleSession.SESSION_TYPE);
        final Date now = new Date();
        this.setStartDate(now);
        this.setEndDate(new Date(now.getTime() + 60 * 60 * 24 * 1000));
    }

    private static class SessionAsset extends AbstractAsset implements LittleSession {

        

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


        @Override
        public LittleSession.Builder copy() {
            return (new SimpleSessionBuilder()).copy( this );
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

