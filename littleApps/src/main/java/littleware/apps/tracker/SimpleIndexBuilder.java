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

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.Serializable;
import java.util.Collection;
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

    
    @Override
    public IndexBuilder put(File localFile) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public IndexBuilder put(String path, long sizeKB) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MemberIndex build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
