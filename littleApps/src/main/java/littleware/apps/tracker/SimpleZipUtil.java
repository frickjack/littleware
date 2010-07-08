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

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import littleware.apps.tracker.MemberIndex.IndexBuilder;
import littleware.base.feedback.Feedback;

/**
 * Check out http://java.sun.com/developer/technicalArticles/Programming/compression/
 */
public class SimpleZipUtil implements ZipUtil {

    private static final Logger log = Logger.getLogger(SimpleZipUtil.class.getName());
    private final Provider<IndexBuilder> provideIndex;

    private static class SimpleInfo implements ZipInfo {

        private final MemberIndex index;
        private final File zipFile;

        public SimpleInfo(MemberIndex index, File zipFile) {
            this.index = index;
            this.zipFile = zipFile;
        }

        @Override
        public MemberIndex getIndex() {
            return index;
        }

        @Override
        public File getZipFile() {
            return zipFile;
        }
    }

    //----------------------------
    @Inject
    public SimpleZipUtil(Provider<MemberIndex.IndexBuilder> provideIndex) {
        this.provideIndex = provideIndex;
    }

    private void zipFile(File file, String zipParent, ZipOutputStream zipOut,
            MemberIndex.IndexBuilder indexBuilder, byte[] buffer) throws IOException {
        final ZipEntry entry = new ZipEntry(file.getName());
        indexBuilder.put(zipParent + file.getName(), file.length() / 1024L);
        zipOut.putNextEntry(entry);
        final BufferedInputStream origin = new BufferedInputStream(
                new FileInputStream(file), buffer.length);
        try {
            for (int count = origin.read(buffer);
                    count != -1;
                    count = origin.read(buffer)) {
                zipOut.write(buffer, 0, count);
            }
        } finally {
            origin.close();
        }
    }

    /**
     * Add the files in the given directory to the zipOut stream,
     * and recurse on directories adding each directory entry relative
     * to zipParent path.
     */
    private void zipDirectory(File directory, String zipParent,
            ZipOutputStream zipOut,
            MemberIndex.IndexBuilder indexBuilder,
            byte[] buffer) throws IOException {
    }

    @Override
    public ZipInfo zip(File source, Feedback fb) throws IOException {
        if (!(source.exists() && source.canRead())) {
            throw new IOException("Cannot read source: " + source.getAbsolutePath());
        }

        final File zipFile = File.createTempFile(source.getName(), "zip");
        final MemberIndex.IndexBuilder indexBuilder = provideIndex.get();
        final byte buffer[] = new byte[102400];
        final FileOutputStream dest = new FileOutputStream(zipFile);
        final CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
        final ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(checksum));

        try {
            //out.setMethod(ZipOutputStream.DEFLATED);
            if (source.isDirectory()) {
                zipDirectory(source, "", zipOut, indexBuilder, buffer);
            } else {
                zipFile(source, "", zipOut, indexBuilder, buffer);
            }
            zipOut.close();
            //System.out.println("checksum:" + checksum.getChecksum().getValue());
            return new SimpleInfo( indexBuilder.build(), zipFile );
        } finally {
            zipOut.close();
        }
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
