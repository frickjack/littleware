/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.internal;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.*;
import littleware.base.ZipUtil;
import littleware.base.feedback.Feedback;

/**
 * Check out http://java.sun.com/developer/technicalArticles/Programming/compression/
 */
public class SimpleZipUtil implements ZipUtil {

    private static final Logger log = Logger.getLogger(SimpleZipUtil.class.getName());


    public SimpleZipUtil(){}

    @Override
    public File zip(File source, Feedback fb) throws IOException {
        final File zipFile = File.createTempFile(source.getName(), ".zip");
        zipFile.delete();
        return zip(source, zipFile, fb);
    }

    private void zipFile(File file, String zipParent, ZipOutputStream zipOut,
             byte[] buffer) throws IOException {
        final ZipEntry entry = new ZipEntry(zipParent + file.getName());
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
            byte[] buffer) throws IOException {
        final String myPath;
        if (directory.getName().isEmpty()) {
            myPath = zipParent;
        } else {
            myPath = zipParent + directory.getName() + "/";
            //final ZipEntry entry = new ZipEntry(zipParent + directory.getName());
            //zipOut.putNextEntry(entry);
        }

        final List<File> subDirs = new ArrayList<File>();
        for (File child : directory.listFiles()) {
            if (child.isDirectory()) {
                subDirs.add(child);
            } else if (child.isFile()) {
                zipFile(child, myPath, zipOut, buffer);
            }
        }
        for (File child : subDirs) {
            zipDirectory(child, myPath, zipOut, buffer);
        }
    }

    @Override
    public File zip(File source, File zipFile, Feedback fb) throws IOException {
        if (!(source.exists() && source.canRead())) {
            throw new IOException("Cannot read source: " + source.getAbsolutePath());
        }
        if (zipFile.exists()) {
            throw new IllegalArgumentException("Destination zip file already exists: " + zipFile.getAbsolutePath());
        }
        if (!zipFile.getName().toLowerCase().endsWith(".zip")) {
            throw new IllegalArgumentException("Zip file must end in .zip: " + zipFile);
        }

        final byte buffer[] = new byte[102400];
        final FileOutputStream dest = new FileOutputStream(zipFile);
        final CheckedOutputStream checksum = new CheckedOutputStream(dest, new Adler32());
        final ZipOutputStream zipOut = new ZipOutputStream(new BufferedOutputStream(checksum));

        try {
            //out.setMethod(ZipOutputStream.DEFLATED);
            if (source.isDirectory()) {
                zipDirectory(source, "", zipOut, buffer);
            } else {
                zipFile(source, "", zipOut, buffer);
            }
            zipOut.close();
            //System.out.println("checksum:" + checksum.getChecksum().getValue());
            return zipFile;
        } finally {
            zipOut.close();
        }
    }

    @Override
    public void unzip(File zipFile, File destination, Feedback fb) throws IOException {
        final ZipFile zip = new ZipFile(zipFile);
        final Enumeration zipEnum = zip.entries();
        final byte[] buffer = new byte[102400];
        while (zipEnum.hasMoreElements()) {
            final ZipEntry entry = (ZipEntry) zipEnum.nextElement();
            final File outFile = new File(destination, entry.getName());
            if (outFile.exists()) {
                throw new IllegalStateException("Unzip dest file already exists: " + outFile);
            }
            log.log(Level.FINE, "Extracting: {0}", entry);
            if (entry.isDirectory()) {
                outFile.mkdirs();
                continue;
            } // else
            outFile.getParentFile().mkdirs();
            final BufferedInputStream in = new BufferedInputStream(zip.getInputStream(entry));
            try {
                final BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(new File(destination, entry.getName())),
                        buffer.length);
                try {
                    for (int count = in.read(buffer);
                            count != -1;
                            count = in.read(buffer)) {
                        out.write(buffer, 0, count);
                    }
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
        }
    }

}
