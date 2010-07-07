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
import java.io.IOException;
import littleware.base.feedback.Feedback;

/**
 * Internal utility to help manage checkin/checkout
 */
public interface ZipUtil {
    public interface ZipInfo {
        public MemberIndex getIndex();
        public File        getZipFile();
    }

    public ZipInfo zip( File source, Feedback fb ) throws IOException;
    public void    unzip( File destination, Feedback fb ) throws IOException;

    public String      pickle( MemberIndex index );
    public MemberIndex unpickle( String pickle );
}
