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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import littleware.apps.tracker.MemberIndex;
import littleware.apps.tracker.MemberIndex.FileInfo;
import littleware.apps.tracker.MemberIndex.IndexBuilder;

public class SimpleIndexBuilder implements MemberIndex.IndexBuilder {
    private static class Index implements MemberIndex, Serializable {
        private ImmutableList<FileInfo> index;
        /** No args for serialization */
        public Index() {}
        public Index( ImmutableList<FileInfo> index ) {
            this.index = index;
        }

        @Override
        public Collection<FileInfo> getIndex() {
            return index;
        }

    }

    private static class Info implements MemberIndex.FileInfo, Serializable {
        private String path;
        private long sizeKB;

        /** For serialization */
        public Info() {}
        public Info( String path, long sizeKB ) {
            this.path = path;
            this.sizeKB = sizeKB;
        }

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public long getSizeKB() {
            return sizeKB;
        }

    }

    final ImmutableList.Builder<FileInfo> builder = new ImmutableList.Builder<FileInfo>();

    @Override
    public IndexBuilder put(File localFile) {
        if ( (! localFile.exists()) && localFile.isFile() ) {
            throw new IllegalArgumentException( "Does not exist as file: " + localFile );
        }
        builder.add( new Info( localFile.getPath(), localFile.length() / 1024L ) );
        return this;
    }

    @Override
    public IndexBuilder put(String path, long sizeKB) {
        builder.add( new Info( path, sizeKB ) );
        return this;
    }

    @Override
    public MemberIndex build() {
        return new Index( builder.build() );
    }

}
