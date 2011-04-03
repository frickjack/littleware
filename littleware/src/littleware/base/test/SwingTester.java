/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import littleware.test.JLittleDialog;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;


import javax.swing.*;


import littleware.base.swing.*;
import littleware.test.LittleTest;

/**
 * Test implementations of littleware.base.swing components
 */
public class SwingTester extends LittleTest {

    private static final Logger log = Logger.getLogger(SwingTester.class.getName() );
    public static final String OS_NEWLINE = System.getProperty("line.separator");

    /**
     * Do nothing constructor - calls through to super
     */
    public SwingTester() {
        setName("testJTextAppender");
    }

    /**
     * Little utility frickjack to maniipulate the JTextAppender
     * while the user views the test
     */
    private class TestAppender implements Runnable {

        private Appendable oappend_test = null;
        private boolean ob_shutdown = false;

        public TestAppender(Appendable append_test) {
            oappend_test = append_test;
        }

        public synchronized void shutdown() {
            if (!ob_shutdown) {
                ob_shutdown = true;
                try {
                    wait();
                } catch (InterruptedException e) {
                    log.log(Level.INFO, "Caught waiting for shutdown: " + e, e);
                }
            }
        }

        @Override
        public void run() {
            try {
                for (int i = 0; !ob_shutdown; ++i) {
                    oappend_test.append("testline: " + i + ", sleep 1 second\n");
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                log.log(Level.INFO, "AppendThread caught: " + e, e);
            } finally {
                synchronized (this) {
                    ob_shutdown = true;
                    notifyAll();
                }
            }
        }
    }

    /**
     * Test the JTextAppender
     */
    public void testJTextAppender() {
        JTextAppender w_appender = new JTextAppender();
        TestAppender run_test = new TestAppender(w_appender);
        try {
            Thread thread_test = new Thread(run_test);
            thread_test.start();

            assertTrue("User says test went OK",
                    JLittleDialog.showTestDialog(w_appender,
                    "Please verify that text is scrolling correctly" +
                    OS_NEWLINE +
                    "Hit OK on success, CANCEL on failure"));
        } finally {
            run_test.shutdown();
        }
    }

    /** Test the ListModelIterator */
    public void testListModelIterator() {
        DefaultListModel lmodel_test = new DefaultListModel();

        for (int i = 0; i < 10; ++i) {
            lmodel_test.addElement(i);
        }

        int i_count = 0;
        for (Iterator<Object> r_i = new ListModelIterator(lmodel_test);
                r_i.hasNext();) {
            int i_check = ((Integer) r_i.next()).intValue();
            assertTrue("iterating ok: " + i_count + "?=" + i_check, i_count == i_check);
            ++i_count;
        }
        assertTrue("iterated to end of list: " + i_count, 10 == i_count);
    }
}
