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

import java.io.File;
import java.util.Collection;

/**
 * Index of the files associated with a member
 */
public interface MemberIndex {
    public interface FileInfo {
        /**
         * Path relative to member package root: bla/foo/bar
         */
        public String getPath();
        public long   getSizeKB();
    }

    public interface IndexBuilder {
        public IndexBuilder put( File localFile );
        public IndexBuilder put( String path, long sizeKB );
        public MemberIndex build();
    }
    
    public Collection<FileInfo>  getIndex();
}
