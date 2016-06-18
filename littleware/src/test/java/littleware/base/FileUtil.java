/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.test;

import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import littleware.base.Whatever;

/**
 * Little utility to help setup, scan, and cleanup a directory tree for tests
 */
public class FileUtil {

    /**
     * Utility builds a little directory tree under root
     *
     * @return the total number of directories and files created under root
     */
    public int buildTestTree(File root) throws IOException {
        File parent = root;
        int count = 0;
        for (int dirCount = 0; dirCount < 3; ++dirCount) {
            final File dir = new File(parent, "ZipUtilTester" + count);
            count = count + 1;
            parent = dir;
            if (!dir.exists()) {
                dir.mkdirs();
            }
            for (int fileCount = 0; fileCount <= dirCount; ++fileCount) {
                final File file = new File(parent, "ZipUtilTester" + count + ".txt");
                count = count + 1;
                if (!file.exists()) {
                    final Writer writer = new FileWriter(file);
                    try {
                        writer.write("bla bla bla\n");
                    } finally {
                        writer.close();
                    }
                }
            }
        }
        return count;
    }

    public List<File> lsR(final File target) {
        final ImmutableList.Builder<File> builder = ImmutableList.builder();
        try {
            return (new Callable<List<File>>() {

                private void call(final File start) {
                    if (!start.exists()) {
                        return;
                    }
                    if (start.isDirectory()) {
                        builder.add(start);
                        for (File scan : start.listFiles()) {
                            call(scan);
                        }
                    } else if (start.isFile()) {
                        builder.add(start);
                    }
                }

                @Override
                public List<File> call() throws Exception {
                    call(target);
                    return builder.build();
                }
            }).call();
        } catch (Exception ex) {
            throw new IllegalStateException("Unexpected", ex);
        }
    }

    /**
     * Recursive delete
     *
     * @param limit on size of tree - throw exception if tree is bigger
     * @throws IOException
     */
    public void deleteR(File root, int limit) throws IOException {
        // cleanup test
        final List<File> deleteList = new ArrayList(lsR(root));
        Collections.reverse(deleteList);
        Whatever.get().check("Should be fewer than 12 files to cleanup under " + root,
                deleteList.size() < 12);
        for (File scan : deleteList) {
            scan.delete();
        }


    }
}
