/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.test;

import com.google.inject.Inject;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import littleware.apps.swingclient.DeleteAssetStrategy;
import littleware.apps.swingclient.JDeleteAssetBuilder;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetTreeTool;
import littleware.asset.AssetType;
import littleware.base.AssertionFailedException;
import littleware.base.EventBarrier;
import littleware.base.Maybe;
import littleware.test.LittleTest;

/**
 * Test the JDeleteAssetStrategy UI
 */
public class JDeleteAssetTester extends LittleTest {

    private static final Logger log = Logger.getLogger(JDeleteAssetTester.class.getName());
    private static final String TestFolder = "JDeleteAssetTester";
    private final JDeleteAssetBuilder builder;
    private final AssetManager manager;
    private final AssetSearchManager search;
    private final AssetPathFactory pathFactory;
    private final AssetTreeTool treeTool;

    @Inject
    public JDeleteAssetTester(JDeleteAssetBuilder builder,
            AssetManager manager,
            AssetSearchManager search,
            AssetPathFactory pathFactory,
            AssetTreeTool treeTool) {
        this.builder = builder;
        this.manager = manager;
        this.search = search;
        this.treeTool = treeTool;
        this.pathFactory = pathFactory;
        setName("testDeleteStrategy");
    }

    /**
     * Create some assets for the test to delete
     */
    @Override
    public void setUp() {
        tearDown();
        try {
            final Asset home = getTestHome(search);
            final Asset aTest = AssetType.createSubfolder(AssetType.GENERIC, TestFolder, home);
            final List<Asset> vCreate = new ArrayList<Asset>();
            vCreate.add(aTest);
            for (String sName : Arrays.asList("A", "B", "C")) {
                final Asset aFolder = AssetType.createSubfolder(AssetType.GENERIC, sName, aTest);
                vCreate.add(aFolder);
                for (String sSuffix : Arrays.asList("A", "B", "C")) {
                    final Asset aChild = AssetType.createSubfolder(AssetType.GENERIC, sName + sSuffix,
                            aFolder);
                    vCreate.add(aChild);
                }
            }
            manager.saveAssetsInOrder(vCreate, "Setup test");
        } catch (Exception ex) {
            log.log(Level.WARNING, "Setup failed", ex);
            fail("Caught: " + ex);
        }
    }

    @Override
    public void tearDown() {
        try {
            final Asset home = getTestHome(search);
            final Maybe<Asset> maybe = search.getAssetAtPath(
                    pathFactory.createPath(home.getObjectId(), TestFolder));
            if (maybe.isSet()) {
                final List<Asset> vDelete = treeTool.loadBreadthFirst(maybe.get().getObjectId());
                Collections.reverse(vDelete);
                for (Asset aDelete : vDelete) {
                    manager.deleteAsset(aDelete.getObjectId(), "test cleanup");
                }
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed", ex);
            fail("Teardown failed: " + ex);
        }
    }

    private static class TestData {

        public final Asset testAsset;
        public final EventBarrier<Boolean> barrier = new EventBarrier<Boolean>();
        public final List<PropertyChangeEvent> eventList = new ArrayList<PropertyChangeEvent>();

        /** Inject test asset */
        public TestData(Asset testAsset) {
            this.testAsset = testAsset;
        }
    }

    public void testDeleteStrategy() {
        final TestData data;
        try {
            data = new TestData(
                    search.getAssetAtPath(
                    pathFactory.createPath(getTestHome(search).getObjectId(), TestFolder)).get());
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to retrieve test asset", ex);
            fail("Exception loading test asset: " + ex);
            throw new AssertionFailedException("Cannot reach here", ex);
        }

        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                @Override
                public void run() {
                    launchUI(data);
                }
            });
        } catch (InterruptedException ex) {
            fail("Test interrupted");
        } catch (InvocationTargetException ex) {
            try {
                throw ex.getTargetException();
            } catch (Error target) {
                throw target;
            } catch (RuntimeException target) {
                throw target;
            } catch (Throwable target) {
                log.log(Level.WARNING, "Caught", target);
                fail("Cuaght: " + target);
            }
        }
        try {
            data.barrier.waitForEventData();
            data.barrier.waitForEventData();

            assertTrue("Property change events fired",
                    (data.eventList.size() > 1) && data.eventList.get(0).getPropertyName().equals("state") && data.eventList.get(0).getNewValue().equals(DeleteAssetStrategy.State.Scanning));
            // Make sure our asset was actually deleted
            assertTrue("Root asset deleted",
                    !search.getAsset(data.testAsset.getObjectId()).isSet());

        } catch (Exception ex) {
            fail("Exception: " + ex);
        }
    }

    private void launchUI(final TestData data) {
        assertTrue("Test running on event dispatch thread",
                SwingUtilities.isEventDispatchThread());
        try {
            final JDeleteAssetBuilder.JDeletePanel delete = builder.build(
                    data.testAsset.getObjectId());

            final JDialog dialog = new JDialog();
            dialog.setModal(false);
            dialog.setTitle("DeleteTester test");

            final PropertyChangeListener listener = new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    log.log(Level.INFO, "Got event: " + evt.getPropertyName() + " with value " + evt.getNewValue());
                    if (evt.getPropertyName().equals("state")) {
                        data.eventList.add(evt);
                        if (((DeleteAssetStrategy.State) evt.getNewValue()).equals(
                                DeleteAssetStrategy.State.Dismissed
                                )) {
                            log.log(Level.INFO, "Dismissing UI");
                            if (!SwingUtilities.isEventDispatchThread()) {
                                SwingUtilities.invokeLater(
                                        new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.setVisible(false);
                                                dialog.dispose();
                                            }
                                        });
                            } else {
                                dialog.setVisible(false);
                                dialog.dispose();
                            }
                        }
                    }
                }
            };

            delete.addPropertyChangeListener(listener);
            assertTrue("Strategy starts in New state",
                    delete.getState().equals(DeleteAssetStrategy.State.New));

            dialog.add(delete);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosed(WindowEvent event) {
                    if ( ! data.barrier.isDataReady() ) {
                        data.barrier.publishEventData(Boolean.TRUE);
                    }
                }
            });

            dialog.pack();
            dialog.setVisible(true);
            delete.launch();
        } catch (Exception ex) {
            log.log(Level.INFO, "Test failed", ex);
            fail("Caught: " + ex);
        }
    }
}