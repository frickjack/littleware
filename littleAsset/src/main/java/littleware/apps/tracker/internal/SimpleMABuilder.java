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

import java.util.UUID;
import littleware.apps.tracker.Member;
import littleware.apps.tracker.MemberAlias;
import littleware.apps.tracker.MemberAlias.MABuilder;
import littleware.apps.tracker.Version;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.Asset;
import littleware.asset.spi.AbstractAssetBuilder;

public class SimpleMABuilder extends AbstractAssetBuilder implements MemberAlias.MABuilder {

    public SimpleMABuilder() {
        super(MemberAlias.MAType);
    }

    @Override
    public MABuilder name(String value) {
        return (MABuilder) super.name(value);
    }

    @Override
    public MABuilder parent(Asset parent) {
        if (!(parent instanceof Version)) {
            throw new IllegalArgumentException("Parent must be a Version");
        }
        return (MABuilder) super.parent(parent);
    }

    @Override
    public MABuilder version(Version parent) {
        return parent(parent);
    }

    @Override
    public MABuilder member(Member member) {
        return (MABuilder) toId(member.getId());
    }

    @Override
    public MABuilder copy(Asset value) {
        return (MABuilder) super.copy(value);
    }

    @Override
    public MemberAlias build() {
        return new SimpleMemberAlias(this);
    }

    private static class SimpleMemberAlias extends AbstractAsset implements MemberAlias {

        private SimpleMemberAlias() {
        }

        private SimpleMemberAlias(SimpleMABuilder builder) {
            super(builder);
        }

        @Override
        public UUID getVersionId() {
            return getFromId();
        }

        @Override
        public UUID getMemberId() {
            return getToId();
        }

        @Override
        public MABuilder copy() {
            return (new SimpleMABuilder()).copy(this);
        }
    }
}
