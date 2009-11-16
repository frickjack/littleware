/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetTreeTool;
import littleware.base.Maybe;
import littleware.base.feedback.Feedback;
import littleware.base.swing.GridBagWrap;
import littleware.base.swing.JTextAppender;

/**
 * A strategy to manage asset-delete user interaction.
 * Client builds strategy via builder, makes the panel
 * visible in the UI, and executes the future.
 * Must allocate on dispatch thread for Swing construction safety.
 */
@Singleton
public class JDeleteAssetBuilder implements DeleteAssetStrategy.Builder {

    private static final Logger log = Logger.getLogger(JDeleteAssetBuilder.class.getName());
    private final AssetTreeTool treeTool;
    private final AssetManager manager;
    private final SwingFeedbackBuilder feedbackBuilder;
    private final ExecutorService executor;
    private final AssetPathFactory pathFactory;

    @Inject
    public JDeleteAssetBuilder(AssetTreeTool treeTool, AssetManager manager,
            SwingFeedbackBuilder feedbackBuilder,
            ExecutorService executor,
            AssetPathFactory pathFactory) {
        this.treeTool = treeTool;
        this.manager = manager;
        this.feedbackBuilder = feedbackBuilder;
        this.executor = executor;
        this.pathFactory = pathFactory;
    }

    @Override
    public JDeletePanel build(UUID uDelete, Feedback feedback) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Not on dispatch thread");
        }
        return new JDeleteAssetStrategy(uDelete, feedback);
    }

    @Override
    public JDeletePanel build(UUID uDelete) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("Not on dispatch thread");
        }
        return new JDeleteAssetStrategy(uDelete);
    }

    /** Little pair container */
    private static class AssetWithPath {

        private final Asset asset;
        private final AssetPath path;

        public AssetWithPath(Asset asset, AssetPath path) {
            this.asset = asset;
            this.path = path;
        }

        public Asset getAsset() {
            return asset;
        }

        private AssetPath getPath() {
            return path;
        }
    }

    public abstract class JDeletePanel extends JPanel implements DeleteAssetStrategy {

        private State state = State.New;
        //private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);

        protected void setState(State state) {
            final State old = this.state;
            this.state = state;
            firePropertyChange("state", old, state);
        }

        @Override
        public State getState() {
            return state;
        }
    }

    private class JDeleteAssetStrategy extends JDeletePanel {

        private static final long serialVersionUID = -6337174636253604664L;
        private final Feedback feedback;
        private final UUID rootId;
        private Maybe<List<AssetWithPath>> maybeAssetList = Maybe.empty();
        private Maybe<? extends Future<?>> maybeRunning = Maybe.empty();
        private final Action deleteAction = new AbstractAction("Ok") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getState().equals(State.Success) || getState().equals(State.Failed) || getState().equals(State.Canceled)) {
                    this.setEnabled(false);
                    feedback.info("UI dismissed - if you still see me, then the application is not hadling dismissal");
                    setState(State.Dismissed);
                    return;
                }
                if ((!getState().equals(State.Ready)) || (!maybeAssetList.isSet()) || maybeAssetList.get().isEmpty()) {
                    feedback.info("Illegal state");
                    throw new IllegalStateException();
                }
                deleteAction.setEnabled(false);
                cancelAction.setEnabled(false);
                maybeRunning = Maybe.something(
                        executor.submit(new Runnable() {

                    @Override
                    public void run() {
                        setState(State.Deleting);
                        final List<AssetWithPath> vDelete = maybeAssetList.get();
                        Collections.reverse(vDelete);
                        feedback.setTitle("Deleting assets ...");
                        feedback.info("Deleting " + vDelete.size() + " assets");
                        try {
                            int iCount = 0;
                            for (AssetWithPath pair : vDelete) {
                                try {
                                    feedback.info("Deleting: " + pair.getPath());
                                    manager.deleteAsset(pair.getAsset().getId(), "Deleting asset");
                                    log.log(Level.FINE, "Setting progress " + iCount++ + "/" + vDelete.size());
                                    feedback.setProgress(iCount, vDelete.size());
                                } catch (Exception ex) {
                                    feedback.info("Failed to delete " + pair.getPath() + ", bailing out: " + ex);
                                    throw ex;
                                }
                            }
                            feedback.info("Delete Complete!");
                            setState(State.Success);
                        } catch (Exception ex) {
                            log.log(Level.WARNING, "Delete failed", ex);
                            feedback.info("Delete failed! " + ex);
                            setState(State.Failed);
                        }
                        maybeRunning = Maybe.empty();
                        SwingUtilities.invokeLater(
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        cancelAction.setEnabled(false);
                                        deleteAction.setEnabled(true);
                                        feedback.info("Press Ok to dismiss");
                                    }
                                });
                    }
                }));
            }
        };
        private final Action cancelAction = new AbstractAction("Cancel") {

            @Override
            public void actionPerformed(ActionEvent e) {
                cancelAction.setEnabled(false);
                if (maybeRunning.isSet() && (!maybeRunning.get().isDone())) {
                    feedback.info("Attempting to cancel running task ...");
                    maybeRunning.get().cancel(true);
                }
                deleteAction.setEnabled(true);
                setState(DeleteAssetStrategy.State.Canceled);
                feedback.info("Delete canceled - press Ok to dismiss");
            }
        };

        /**
         * Build UI - possibly include feedback progress bar and text-appender
         *
         * @param maybeProgress
         * @param maybeAppender
         */
        private void buildUI(Maybe<JProgressBar> maybeProgress,
                Maybe<JTextAppender> maybeAppender) {
            deleteAction.setEnabled(false);
            cancelAction.setEnabled(false);
            final GridBagWrap grid = GridBagWrap.wrap(this);
            grid.gridwidth(GridBagConstraints.REMAINDER).fillX().
                    add(new JLabel("Delete Asset"));
            if (maybeProgress.isSet()) {
                grid.newRow().add(maybeProgress.get());
            }
            if (maybeAppender.isSet()) {
                grid.newRow().gridheight(10).fillBoth().add(maybeAppender.get());
            }
            grid.newRow().fillNone().gridheight(1).gridwidth(1).add(new JButton(deleteAction)).nextCol().nextCol().add(new JButton(cancelAction)).newRow();
        }

        public JDeleteAssetStrategy(UUID rootId) {
            this.rootId = rootId;
            final JProgressBar jprogress = new JProgressBar(0, 100);
            final JTextAppender jappender = new JTextAppender();
            this.feedback = feedbackBuilder.build(jprogress, new JLabel("ignore this"), jappender, log);
            buildUI(Maybe.something(jprogress), Maybe.something(jappender));
        }

        public JDeleteAssetStrategy(UUID rootId, Feedback feedback) {
            this.rootId = rootId;
            this.feedback = feedback;
            final Maybe<JProgressBar> maybeProgress = Maybe.empty();
            final Maybe<JTextAppender> maybeAppender = Maybe.empty();
            buildUI(maybeProgress, maybeAppender);
        }

        /**
         * This kicks off the delete-asset state machine
         * @param executor
         */
        @Override
        public void launch() {
            if (!getState().equals(State.New)) {
                throw new IllegalStateException("Strategy already launched");
            }
            cancelAction.setEnabled(false);
            setState(State.Scanning);
            maybeRunning = Maybe.something(
                    executor.submit(
                    new Runnable() {

                        @Override
                        public void run() {
                            feedback.info("Scanning asset tree under " + rootId + " for asset list to delete");
                            try {
                                final List<Asset> vAsset = treeTool.loadBreadthFirst(rootId, feedback);
                                if (!getState().equals(State.Scanning)) {
                                    // assume things were canceled someplace along the line
                                    setState(State.Canceled);
                                    return;
                                }
                                if (vAsset.isEmpty()) {
                                    feedback.info("No assets found to delete - finished!");
                                    setState(State.Success);
                                    return;
                                }
                                final List<AssetWithPath> vResult = new ArrayList<AssetWithPath>();
                                for (Asset asset : vAsset) {
                                    final AssetPath path = pathFactory.toRootedPath(
                                            pathFactory.createPath(asset.getId()));
                                    feedback.info(path.toString());
                                    vResult.add(new AssetWithPath(asset, path));
                                }
                                feedback.info(Integer.toString(vAsset.size()) + " assets ready to delete: ");

                                maybeAssetList = Maybe.something(vResult);
                                if (getState().equals(DeleteAssetStrategy.State.Scanning)) {
                                    // otherwise probably canceled
                                    setState(State.Ready);
                                }
                            } catch (Exception ex) {
                                if (getState().equals(DeleteAssetStrategy.State.Scanning)) {
                                    setState(State.Failed);
                                }
                                log.log(Level.WARNING, "Scan failed", ex);
                                feedback.log(Level.WARNING, "Delete tree scan failed: " + ex);
                            } finally {
                                SwingUtilities.invokeLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        if ( getState().equals( State.Ready ) ) {
                                            deleteAction.setEnabled(true);
                                            cancelAction.setEnabled(true);
                                        } else {
                                            deleteAction.setEnabled(true);
                                            cancelAction.setEnabled( false );
                                        }
                                    }
                                });

                            }
                        }
                    }));
        }

        @Override
        public UUID getDeleteId() {
            return rootId;
        }
    }
}
