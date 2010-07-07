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
 * Check out http://java.sun.com/developer/technicalArticles/Programming/compression/
 */
public class SimpleZipUtil implements ZipUtil {

    @Override
    public ZipInfo zip(File source, Feedback fb) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void unzip(File destination, Feedback fb) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String pickle(MemberIndex index) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public MemberIndex unpickle(String pickle) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
