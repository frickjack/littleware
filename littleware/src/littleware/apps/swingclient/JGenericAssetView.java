/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.UUID;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.*;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.base.UUIDFactory;
import littleware.apps.swingclient.event.*;
import littleware.base.swing.GridBagWrap;

/** 
 * Simple JPanel based view of a generic asset
 */
public class JGenericAssetView extends JPanel implements AssetView {

    private final static Logger olog = Logger.getLogger(JGenericAssetView.class.getName());
    private static final long serialVersionUID = -7321436305465982373L;
    private final AssetRetriever om_retriever;
    private final AbstractAssetView oview_util = new AbstractAssetView(this) {

        /** Events form the data model */
        @Override
        public void eventFromModel(LittleEvent evt_from_model) {
            JGenericAssetView.this.eventFromModel(evt_from_model);
        }
    };


    {
        oview_util.setFeedback( new LoggerUiFeedback( olog ) );

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
    private final IconLibrary olib_icon;
    /** Panel to stuff summary data into */
    private final JPanel owpanel_summary = new JPanel(new GridBagLayout());
    /** Panel to stuff info that can be toggled visible/invisible */
    private final JPanel owpanel_details = new JPanel(new GridBagLayout());
    private final JPanel owpanel_update = new JPanel(new GridBagLayout());
    // Setup a button to toggle the detals panel visible/invisible
    private final JButton owbutton_details = new JButton("+");


    {
        owbutton_details.setToolTipText("View/Hide Asset details");
    }
    /**
     * Each JLabel holds a piece of data about the asset.
     * Some of the labels act as links to other assets.
     */
    private final JLabel owlabel_type = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JAssetLink owlink_name;
    private final JLabel owlabel_id = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JLabel owlabel_value = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JLabel owlabel_date_created = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JLabel owlabel_date_updated = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JLabel owlabel_start = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JLabel owlabel_end = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JLabel owlabel_transaction = new JLabel("uninitialized", SwingConstants.LEFT);
    private final JAssetLink owlink_acl;
    private final JAssetLink owlink_to;
    private final JAssetLink owlink_from;
    private final JAssetLink owlink_owner;
    private final JAssetLink owlink_home;
    private final JAssetLink owlink_creator;
    private final JAssetLink owlink_updater;
    private final JTextArea owtext_comment = new JTextArea(5, 40);
    private final JTextArea owtext_update = new JTextArea(5, 40);
    private final JTextArea owtext_data = new JTextArea(5, 40);


    {
        for (JTextArea jarea : Arrays.asList(owtext_comment, owtext_update, owtext_data)) {
            jarea.setLineWrap(true);
            jarea.setEditable(false);
        }
    }
    private final JTabbedPane owtab_stuff = new JTabbedPane();

    /**
     * Return a listener that just propagates whatever events it
     * receives to the listeners registered on this view.
     */
    protected LittleListener getListenBridge() {
        return olisten_bridge;
    }
    /**
     * Listener just propogates NavRequestEvents from JAssetLink through
     * to this object&apos;s listeners.
     */
    private LittleListener olisten_bridge = new LittleListener() {

        @Override
        public void receiveLittleEvent(LittleEvent event_little) {
            if (event_little instanceof NavRequestEvent) {
                //event_little.setSource ( JGenericAssetView.this );
                NavRequestEvent event_nav = (NavRequestEvent) event_little;
                fireLittleEvent(new NavRequestEvent(JGenericAssetView.this,
                        event_nav.getDestination(),
                        event_nav.getNavMode()));
            }
        }
    };

    @Override
    public Feedback getFeedback() {
        return oview_util.getFeedback();
    }

    @Override
    public void setFeedback( Feedback feedback ) {
        oview_util.setFeedback( feedback );
    }

    /** Little pair class */
    private static class NameWidget {

        private final String osName;
        private final Component ojWidget;

        public NameWidget(String sName, Component jWidget) {
            osName = sName;
            ojWidget = jWidget;
        }

        public String getName() {
            return osName;
        }

        public Component getWidget() {
            return ojWidget;
        }
    }

    /**
     * Build the UI.
     * Subtypes may override or extend.
     */
    protected void buildAssetUI() {
        //this.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
        this.setLayout(new GridBagLayout());

        //owpanel_details.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );

        for (JAssetLink jlink : Arrays.asList(owlink_home, owlink_from, owlink_to,
                owlink_owner, owlink_acl, owlink_updater,
                owlink_creator)) {
            jlink.addLittleListener(olisten_bridge);
        }

        {
            GridBagWrap gw = GridBagWrap.wrap(owpanel_summary);
            for (NameWidget pair : Arrays.asList(
                    new NameWidget("Home", owlink_home),
                    new NameWidget("From", owlink_from),
                    new NameWidget("To", owlink_to),
                    new NameWidget("Owner", owlink_owner),
                    new NameWidget("Acl", owlink_acl))) {
                gw.add(new JLabel(pair.getName() + ":", SwingConstants.RIGHT)).
                        nextCol().remainderX().fillX().
                        add(pair.getWidget()).
                        fillNone().gridwidth(1).newRow();
            }
            final JScrollPane wScroll = new JScrollPane(owtext_comment);
            wScroll.setPreferredSize(new Dimension(500, 100));
            gw.add(new JLabel("Comment:", SwingConstants.RIGHT)).
                    newRow().fillBoth().remainderX().remainderY().
                    add(wScroll);
        }

        { // Fill in the details panel
            GridBagWrap gw = GridBagWrap.wrap(owpanel_details);
            for (NameWidget pair : Arrays.asList(
                    new NameWidget("Start", owlabel_start),
                    new NameWidget("End", owlabel_end),
                    new NameWidget("Transaction", owlabel_transaction),
                    new NameWidget("Id", owlabel_id),
                    new NameWidget("Value", owlabel_value),
                    new NameWidget("Creator", owlink_creator),
                    new NameWidget("Create date", owlabel_date_created))) {
                gw.add(new JLabel(pair.getName() + ":", SwingConstants.RIGHT)).
                        nextCol().remainderX().fillX().
                        add(pair.getWidget()).
                        fillNone().gridwidth(1).newRow();
            }
            final JScrollPane wScroll = new JScrollPane(owtext_data);
            wScroll.setPreferredSize(new Dimension(500, 100));

            gw.add(new JLabel("Data block: ")).
                    newRow().fillBoth().remainderX().remainderY().
                    add(wScroll);
        }
        {
            GridBagWrap gw = GridBagWrap.wrap(owpanel_update);
            for (NameWidget pair : Arrays.asList(
                    new NameWidget("Last updater", owlink_updater),
                    new NameWidget("Last update date", owlabel_date_updated))) {
                gw.add(new JLabel(pair.getName() + ":", SwingConstants.RIGHT)).
                        nextCol().remainderX().fillX().
                        add(pair.getWidget()).
                        fillNone().gridwidth(1).newRow();
            }
            final JScrollPane wScroll = new JScrollPane(owtext_update);
            wScroll.setPreferredSize(new Dimension(500, 100));

            gw.add(new JLabel("Last update comment: ")).
                    newRow().fillBoth().remainderX().remainderY().
                    add(wScroll);
        }

        owtab_stuff.add("Summary", owpanel_summary);
        owtab_stuff.add("Details", owpanel_details);
        owtab_stuff.add("Update", owpanel_update);

        // Asset type and icon
        //grid_control.gridwidth = GridBagConstraints.REMAINDER;
        GridBagWrap gw = GridBagWrap.wrap(this);
        gw.fillBoth().remainderX().add(owlink_name).
                newRow().gridheight(1).
                add(owlabel_type);
        gw.newRow().remainderX().remainderY().
                fillBoth().
                add(owtab_stuff);
    }

    /**
     * Inject dependencies.
     *
     * @param model_asset to view initially
     * @param m_retriever to retrieve asset details with
     * @param lib_icon icon source
     * @param m_thumb thumbnail source
     */
    @Inject
    protected JGenericAssetView(
            AssetRetriever m_retriever,
            IconLibrary lib_icon,
            Provider<JAssetLink> provideLinkView) {
        owlink_name = provideLinkView.get();
        om_retriever = m_retriever;
        olib_icon = lib_icon;
        owlink_acl = provideLinkView.get();
        owlink_to = provideLinkView.get();
        owlink_from = provideLinkView.get();
        owlink_owner = provideLinkView.get();
        owlink_home = provideLinkView.get();
        owlink_creator = provideLinkView.get();
        owlink_updater = provideLinkView.get();

        buildAssetUI();
    }

    /**
     * Trigger a UI sync call to updateAssetUI
     * if the LittleEvent comes from
     * the getAssetModel() AssetModel (data model update).
     */
    protected void eventFromModel(final LittleEvent event ) {
        if (event .getSource() == getAssetModel()) {
            // Model has changed under us
            SwingUtilities.invokeLater(
                    new Runnable() {
                        @Override
                        public void run() {
                            if ( event.getOperation().equals( AssetModel.Operation.assetDeleted.toString() ) ) {
                                final AssetModelLibrary lib = getAssetModel().getLibrary();
                                final Asset    deleted = getAssetModel().getAsset();
                                final UUID     uNew = (deleted.getFromId() != null) ?
                                    deleted.getFromId() : deleted.getHomeId();
                                getFeedback().info( "Asset in view deleted, moving to view " + uNew );
                                try {
                                    setAssetModel( lib.retrieveAssetModel(uNew, om_retriever));
                                } catch ( Exception ex ) {
                                    getFeedback().log( Level.WARNING, "Failed to update view after asset delete: " + ex );
                                    olog.log( Level.WARNING, "Failed update after asset delete: " + ex );
                                }
                            } else {
                                updateAssetUI();
                            }
                        }
                    });
        }
    }

    /**
     * Give subtypes access to SimpleLittleTool for
     * firing PropertyChangeEvents and LittleEvents to
     * registered listeners
     *
     * @return SimpleLittleTool managing listeners.
     */
    protected SimpleLittleTool getEventTool() {
        return oview_util;
    }

    /**
     * Give subtypes access to add into the tab-pain.
     *
     * @param i_index may be -1 to just append to end
     */
    protected void insertTab(String s_title, Icon icon_title,
            Component w_tab, String s_tip,
            int i_index) {
        if (i_index < 0) {
            i_index = owtab_stuff.getTabCount();
        }
        owtab_stuff.insertTab(s_title, icon_title, w_tab, s_tip, i_index);
    }

    /**
     * Let subtypes disable their tab
     *
     * @param w_tab component corresponding to the tab to 
     *                 disable/enable
     * @param b_enable set true to enable, false to disable
     */
    protected void setTabEnabled(Component w_tab, boolean b_enable) {
        final int i_index = owtab_stuff.indexOfComponent(w_tab);
        if (i_index < 0) {
            return;
        }
        owtab_stuff.setEnabledAt(i_index, b_enable);
        if ((!b_enable) && (i_index == owtab_stuff.getSelectedIndex())) {
            for (int i = 1; i < owtab_stuff.getTabCount(); ++i) {
                final int iTab = (i_index + i) % owtab_stuff.getTabCount();
                if (owtab_stuff.isEnabledAt(iTab)) {
                    owtab_stuff.setSelectedIndex(iTab);
                    return;
                }
            }
        }
    }

    /**
     * Update a label holding Date info
     *
     * @param wlabel_date to update
     * @param t_date to display - may be null
     */
    private void updateLabelInfo(JLabel wlabel_date, Date t_date) {
        if (null == t_date) {
            wlabel_date.setText("<html><i>null</i></html>");
        } else {
            DateFormat format_date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            wlabel_date.setText(format_date.format(t_date));
        }
        wlabel_date.setIcon(olib_icon.lookupIcon("littleware.calendar"));
    }

    /**
     * Update the JLabel that shows the current view&apos;s dependence
     * on some other asset.
     *
     * @param wlink_depend the JLabel that needs update
     * @param u_depend id of the asset this depends on - will lookup that
     *                          asset to get its name if possible
     */
    private void updateLabelInfo(JAssetLink wlink_depend, UUID u_depend) {
        wlink_depend.setLink(u_depend);
    }

    /**
     * Reset the entire UI with fresh data from the active model.
     * Subtypes may extend, but should start out by calling super.updateAssetUI().
     */
    protected void updateAssetUI() {
        // Reconfigure the UI
        Asset a_data = getAssetModel().getAsset();
        owlabel_type.setText(a_data.getAssetType().toString());
        owlabel_type.setIcon(olib_icon.lookupIcon(a_data));

        owlink_name.setLink(a_data);
        owlabel_id.setText(UUIDFactory.makeCleanString(a_data.getObjectId()));
        owlabel_value.setText(a_data.getValue().toString());
        owlabel_transaction.setText(Long.toString(a_data.getTransactionCount()));

        updateLabelInfo(owlink_acl, a_data.getAclId());
        updateLabelInfo(owlink_to, a_data.getToId());
        updateLabelInfo(owlink_from, a_data.getFromId());
        updateLabelInfo(owlink_home, a_data.getHomeId());
        updateLabelInfo(owlink_owner, a_data.getOwnerId());
        updateLabelInfo(owlink_creator, a_data.getCreatorId());
        updateLabelInfo(owlink_updater, a_data.getLastUpdaterId());

        updateLabelInfo(owlabel_start, a_data.getStartDate());
        updateLabelInfo(owlabel_end, a_data.getEndDate());
        updateLabelInfo(owlabel_date_updated, a_data.getLastUpdateDate());
        updateLabelInfo(owlabel_date_created, a_data.getCreateDate());

        owtext_comment.setText(a_data.getComment());
        owtext_update.setText(a_data.getLastUpdate());
        owtext_data.setText(a_data.getData());

        this.repaint();
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
}

