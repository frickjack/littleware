/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.test;

import com.google.inject.Inject;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import littleware.apps.swingbase.controller.ShutdownHandler;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.view.BaseView;
import littleware.apps.swingbase.view.BaseView.ViewBuilder;
import littleware.base.EventBarrier;
import littleware.base.swing.GridBagWrap;
import littleware.test.LittleTest;

/**
 * Just popup a simple do-nothing swing.base app
 */
public class SwingBaseTester extends LittleTest {

    private final static Logger log = Logger.getLogger(SwingBaseTester.class.getName());
    private final BaseData model;
    private final ViewBuilder viewBuilder;

    @Inject
    public SwingBaseTester(BaseData model, BaseView.ViewBuilder viewBuilder) {
        this.model = model;
        this.viewBuilder = viewBuilder;
        setName( "testSwingBase" );
    }

    enum TestResult {

        Passed, Failed
    }

    private void launchView(final EventBarrier<TestResult> barrier) {
        try {
            if (!SwingUtilities.isEventDispatchThread()) {
                throw new IllegalStateException("Not running on dispatch thread");
            }
            final JFrame container = new JFrame();
            final JPanel jcontent = new JPanel();
            final JButton jbuttonPassed = new JButton(new AbstractAction("Passed") {

                @Override
                public void actionPerformed(ActionEvent event) {
                    barrier.publishEventData(TestResult.Passed);
                    container.dispose();
                }
            });
            final JButton jbuttonFailed = new JButton(new AbstractAction("Failed") {

                @Override
                public void actionPerformed(ActionEvent event) {
                    barrier.publishEventData(TestResult.Failed);
                    container.dispose();
                }
            });

            final GridBagWrap gb = GridBagWrap.wrap(jcontent);
            gb.fillBoth().gridheight(2).remainderX().add(
                    new JLabel("<html><p>Does the test pass or fail ? ...............................................................................................................................................................</p></html>")).
                    newRow();
            gb.gridwidth(1).gridheight(1).fillNone().add(jbuttonPassed).nextCol().
                    add(jbuttonFailed);
            final Reference ref;

            viewBuilder.basicContent(jcontent).model(model).
                    windowCloseHandler( new ShutdownHandler(){
                @Override
                public void requestShutdown() {
                    // do not actually shutdown, just end the test!
                    barrier.publishEventData(TestResult.Failed);
                    container.dispose();
                }
            } ).container( container ).build();
            container.setVisible(true);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Caught exception", ex);
            barrier.publishEventData(TestResult.Failed);
        }
    }

    public void testSwingBase() {
        final EventBarrier<TestResult> barrier = new EventBarrier<TestResult>();
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                launchView(barrier);
            }
        });
        try {
            assertTrue("User indicates test passed",
                    barrier.waitForEventData().equals(TestResult.Passed));
        } catch (InterruptedException ex) {
            fail("Caught exception: " + ex);
        }
    }
}
