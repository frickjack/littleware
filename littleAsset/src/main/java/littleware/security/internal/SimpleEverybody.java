/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import littleware.asset.LittleHome;
import littleware.asset.spi.AbstractAsset;
import littleware.base.AssertionFailedException;
import littleware.security.AccountManager;
import littleware.security.Everybody;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;

/**
 * Special implementation of LittleGroup that includes everybody
 */
@Singleton
public class SimpleEverybody extends AbstractAsset implements Everybody {
    private static final Date d1970;

    static {
        try {
            d1970 = (new SimpleDateFormat("yyyy")).parse("1970");
        } catch (Exception ex) {
            throw new AssertionFailedException("Failed setup", ex);
        }
    }
    private static final ImmutableMap<String, String> emptyAttributeMap = ImmutableMap.of();
    private static final ImmutableMap<String, Date> emptyDateMap = ImmutableMap.of();
    private static final ImmutableMap<String, UUID> emptyLinkMap = ImmutableMap.of();
    
    /**
     * Be careful about this crazy singleton here ... the constructor requires the other statics!
     */
    public static Everybody singleton = new SimpleEverybody();
    
    public SimpleEverybody() {
        super(LittleGroup.GROUP_TYPE,
                AccountManager.UUID_EVERYBODY_GROUP,
                LittleHome.LITTLE_HOME_ID,
                AccountManager.UUID_ADMIN,
                LittleHome.LITTLE_HOME_ID, null, null, 0L,
                "group.littleware.everybody",
                0, d1970,
                AccountManager.UUID_ADMIN, "",
                d1970,
                AccountManager.UUID_ADMIN, "", null, null, 0.0f, "",
                emptyAttributeMap,
                emptyDateMap,
                emptyLinkMap
                );
    }

    @Override
    public Collection<LittlePrincipal> getMembers() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Always returns rue
     */
    @Override
    public boolean isMember(LittlePrincipal member) {
        return true;
    }

    @Override
    public LittleGroup.Builder copy() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
