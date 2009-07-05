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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import littleware.apps.client.AbstractAssetView;
import littleware.apps.client.AssetModel;
import littleware.apps.client.AssetModelLibrary;
import littleware.apps.client.AssetView;
import littleware.apps.client.Feedback;
import littleware.apps.client.LittleEvent;
import littleware.apps.client.LittleListener;
import littleware.apps.swingclient.event.NavRequestEvent;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.base.swing.JUtil;

/**
 * Limited Outliner view of an asset's neighbors
 */
public class JAssetFamilyView extends JPanel implements AssetView {

    private static final Logger olog = Logger.getLogger(JAssetFamilyView.class.getName());
    private static final long serialVersionUID = -923629631052309532L;
    private final AbstractAssetView oview_util = new AbstractAssetView(this) {

        /** Events form the data model */
        @Override
        public void eventFromModel(LittleEvent evt_from_model) {
            JAssetFamilyView.this.eventFromModel(evt_from_model);
        }
    };


    {
        // Should only happen on call to setAssetModel ...
        oview_util.addPropertyChangeListener(new PropertyChangeListener() {

            /** Receive events from the View model */
            @Override
            public void propertyChange(PropertyChangeEvent evt_prop) {
                if (evt_prop.getPropertyName().equals(AssetView.Property.assetModel.toString())) {
                    // Model has changed under us
                    SwingUtilities.invokeLater(
                            new Runnable() {

                                @Override
                                public void run() {
                                    updateAssetUI();
                                }
                            });
                }
            }
        });
    }
    //private final DefaultMutableTreeNode onodeRoot = new DefaultMutableTreeNode( "Root" );
    private final JTree ojTree = new JTree(); //onodeRoot, true );

    {
        MouseListener ml = new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                int selRow = ojTree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = ojTree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() > 0) {
                        //myDoubleClick(selRow, selPath);
                        final Object data = ((DefaultMutableTreeNode) selPath.getLastPathComponent()).getUserObject();
                        if (null == data) {
                            // do nothing
                        } else if (data instanceof UUID) {
                            oview_util.fireLittleEvent(
                                    new NavRequestEvent(JAssetFamilyView.this,
                                    (UUID) data, NavRequestEvent.NavMode.GENERIC));
                        } else {
                            oview_util.fireLittleEvent(
                                    new NavRequestEvent(JAssetFamilyView.this,
                                    ((Asset) data).getObjectId(),
                                    NavRequestEvent.NavMode.GENERIC));
                        }
                    }
                }
            }
        };


        ojTree.addMouseListener(ml);
        ojTree.setRootVisible(false);

        ojTree.addTreeWillExpandListener(new TreeWillExpandListener() {

            @Override
            public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                final DefaultMutableTreeNode node = (DefaultMutableTreeNode) event.getPath().getLastPathComponent();
                if ((node.getChildCount() > 0) || (!node.getAllowsChildren())) {
                    // already expanded
                    return;
                }
                Object info = node.getUserObject();
                if (null == info) {
                    // frick
                    node.setAllowsChildren(false);
                    return;
                } else if (info instanceof UUID) {
                    addChildren(node, (UUID) info);
                } else {
                    addChildren(node, ((Asset) info).getObjectId());
                }
                Component w_root = JUtil.findRoot(JAssetFamilyView.this);
                w_root.validate();
            }

            @Override
            public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException {
                // nothing special
            }
        });
        ojTree.setExpandsSelectedPaths(true);
    }
    private final AssetSearchManager osearch;
    private final AssetModelLibrary olibAsset;

    private void buildUI() {
        this.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        ojTree.setBackground( null );
        final JScrollPane scrollTree = new JScrollPane( ojTree );
        scrollTree.setPreferredSize( new Dimension( 500, 500 ) );
        this.add( scrollTree );
    }

    @Inject
    public JAssetFamilyView(JAssetLinkRenderer render,
            AssetModelLibrary libAsset,
            AssetSearchManager search) {
        render.setRenderThumbnail(false );
        ojTree.setCellRenderer(render);
        osearch = search;
        olibAsset = libAsset;
        buildUI();
    }

    /**
     * The renderer for the underlying JTree
     */
    public void setCellRenderer( TreeCellRenderer render ) {
        ojTree.setCellRenderer( render );
    }
    public TreeCellRenderer getCellRenderer() {
        return ojTree.getCellRenderer();
    }

    /**
     * Trigger a UI sync call to updateAssetUI
     * if the LittleEvent comes from
     * the getAssetModel() AssetModel (data model update).
     */
    protected void eventFromModel(LittleEvent evt_prop) {
        SwingUtilities.invokeLater(
                new Runnable() {

                    @Override
                    public void run() {
                        updateAssetUI();
                    }
                });

    }

    /**
     * Register the parent's children with it's parent node
     *
     * @param nodeParent
     * @param uParent
     * @return mapping of children to the node in the tree
     */
    protected Map<UUID, DefaultMutableTreeNode> addChildren(DefaultMutableTreeNode nodeParent,
            UUID uParent) {
        final Map<UUID, DefaultMutableTreeNode> mapNode =
                new HashMap<UUID, DefaultMutableTreeNode>();
        try {
            final List<Asset> vChildren = new ArrayList<Asset>();
            for (UUID uChild : osearch.getAssetIdsFrom(uParent, null).values()) {
                final AssetModel modChild = olibAsset.retrieveAssetModel(uChild, osearch);
                if (null != modChild) {
                    vChildren.add(modChild.getAsset());
                }
            }
            if (vChildren.isEmpty()) {
                nodeParent.setAllowsChildren(false);
                return mapNode;
            }
            Collections.sort(vChildren, new Comparator<Asset>() {

                @Override
                public int compare(Asset o1, Asset o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
            for (Asset aChild : vChildren) {
                final DefaultMutableTreeNode node = new DefaultMutableTreeNode(aChild);
                olog.log(Level.FINE, "Adding node " + aChild.getName());
                nodeParent.add(node);
                mapNode.put(aChild.getObjectId(), node);
            }
        } catch (Exception ex) {
            olog.log(Level.WARNING, "Failed populated family tree", ex);
        }
        return mapNode;
    }

    /** Update the UI with current data - runs on event dispatch thread */
    private void updateAssetUI() {
        final DefaultMutableTreeNode nodeRoot = new DefaultMutableTreeNode("Root");
        final DefaultTreeModel model = new DefaultTreeModel(nodeRoot, true);

        if (null == getAssetModel()) {
            ojTree.setModel(model);
            return;
        }
        final Asset aView = getAssetModel().getAsset();
        olog.log(Level.FINE, "have view ...");
        final DefaultMutableTreeNode nodeParent = new DefaultMutableTreeNode(aView.getFromId());
        nodeRoot.add(nodeParent);
        DefaultMutableTreeNode nodeMe = null;

        if (null != aView.getFromId()) {
            nodeMe = addChildren(nodeParent, aView.getFromId()).get(aView.getObjectId());
        }
        if (null == nodeMe) {
            nodeMe = new DefaultMutableTreeNode(aView);
            nodeParent.add(nodeMe);
        }
        addChildren(nodeMe, aView.getObjectId());

        ojTree.setModel(model);
        final TreePath pathMe = new TreePath(new Object[]{nodeRoot, nodeParent, nodeMe});
        ojTree.setSelectionPath(pathMe);
        ojTree.expandPath(pathMe);
    }

    @Override
    public AssetModel getAssetModel() {
        return oview_util.getAssetModel();
    }

    @Override
    public void setAssetModel(AssetModel model_asset) {
        oview_util.setAssetModel(model_asset);
    }

    @Override
    public void addLittleListener(LittleListener listen_little) {
        oview_util.addLittleListener(listen_little);
    }

    @Override
    public void removeLittleListener(LittleListener listen_little) {
        oview_util.removeLittleListener(listen_little);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listen_props) {
        oview_util.addPropertyChangeListener(listen_props);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listen_props) {
        oview_util.removePropertyChangeListener(listen_props);
    }

    /**
     * Allow subtypes to fire events to the listeners managed by this class.
     *
     * @param event_little to propogate to listeners
     */
    protected void fireLittleEvent(LittleEvent event_little) {
        oview_util.fireLittleEvent(event_little);
    }

    @Override
    public Feedback getFeedback() {
        return oview_util.getFeedback();
    }

    @Override
    public void setFeedback( Feedback feedback ) {
        oview_util.setFeedback( feedback );
    }

}
