/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;

/**
 *
 * @author pasquini
 */
public interface Member extends Asset {
    /** Alias for getFromId */
    public UUID getVersionId();

    @Override
    public MemberBuilder copy();
    
    public interface MemberBuilder extends AssetBuilder {
        @Override
        public MemberBuilder copy( Asset value );
        /**
         * IllegalArgumentException if value is not a Version
         */
        @Override
        public MemberBuilder parent( Asset value );
        /**
         * Alias for parent
         */
        public MemberBuilder version( Version value );
        @Override
        public Member build();
    }
}
