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

    /**
     * Zip the given source to a temp zip file
     *
     * @param source file or directory to zip up
     * @param fb to report progress
     * @return information about the generated zip
     */
    public ZipInfo zip( File source, Feedback fb ) throws IOException;
    /**
     * Allow the user to specify the output file name
     * @param source
     * @param zipFile destination should have name "bla.zip" - throws exception if already exists
     */
    public ZipInfo zip( File source, File zipFile, Feedback fb ) throws IOException;
    public void    unzip( File zipFile, File destination, Feedback fb ) throws IOException;

    public String      pickle( MemberIndex index );
    public MemberIndex unpickle( String pickle );
}
