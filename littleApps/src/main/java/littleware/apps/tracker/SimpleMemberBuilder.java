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
import littleware.apps.tracker.Member.MemberBuilder;
import littleware.asset.Asset;
import littleware.asset.SimpleAssetBuilder;

public class SimpleMemberBuilder extends SimpleAssetBuilder implements MemberBuilder {

    public SimpleMemberBuilder() {
        super(Member.MemberType);
    }

    @Override
    public MemberBuilder name(String value) {
        return (MemberBuilder) super.name(value);
    }

    @Override
    public MemberBuilder copy(Asset value) {
        return (MemberBuilder) super.copy(value);
    }

    @Override
    public MemberBuilder parent(Asset value) {
        if (!(value instanceof Version)) {
            throw new IllegalArgumentException("Member parent must be a Version node");
        }
        return (MemberBuilder) super.parent(value);
    }

    @Override
    public MemberBuilder version(Version value) {
        return parent(value);
    }

    @Override
    public Member build() {
        return new SimpleMember(this);
    }

    private static class SimpleMember extends SimpleAsset implements Member {

        private SimpleMember() {
        }

        private SimpleMember(SimpleMemberBuilder builder) {
            super(builder);
        }

        @Override
        public UUID getVersionId() {
            return getFromId();
        }

        @Override
        public MemberBuilder copy() {
            return (new SimpleMemberBuilder()).copy(this);
        }
    }
}
