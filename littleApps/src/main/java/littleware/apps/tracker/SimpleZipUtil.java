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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import littleware.base.feedback.Feedback;

/**
 * Check out http://java.sun.com/developer/technicalArticles/Programming/compression/
 */
public class SimpleZipUtil implements ZipUtil {
    private static class SimpleInfo implements ZipInfo {
        private final MemberIndex index;
        private final File zipFile;
        public SimpleInfo( MemberIndex index, File zipFile ) {
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

    @Override
    public ZipInfo zip(File source, Feedback fb) throws IOException {
      try {
         BufferedInputStream origin = null;
         FileOutputStream dest = new
           FileOutputStream("c:\\zip\\myfigs.zip");
         CheckedOutputStream checksum = new
           CheckedOutputStream(dest, new Adler32());
         ZipOutputStream out = new
           ZipOutputStream(new
             BufferedOutputStream(checksum));
         //out.setMethod(ZipOutputStream.DEFLATED);
         final int BUFFER = 102400;
         byte data[] = new byte[BUFFER];
         // get a list of files from current directory
         File f = new File(".");
         String files[] = f.list();

         for (int i=0; i<files.length; i++) {
            System.out.println("Adding: "+files[i]);
            FileInputStream fi = new
              FileInputStream(files[i]);
            origin = new
              BufferedInputStream(fi, BUFFER);
            ZipEntry entry = new ZipEntry(files[i]);
            out.putNextEntry(entry);
            int count;
            while((count = origin.read(data, 0,
              BUFFER)) != -1) {
               out.write(data, 0, count);
            }
            origin.close();
         }
         out.close();
         System.out.println("checksum:"+checksum.getChecksum().getValue());
      } catch(Exception e) {
         e.printStackTrace();
      }
      return null;
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
