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
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.util.*;
import java.util.logging.Logger;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import littleware.asset.AssetType;
import littleware.base.event.LittleListener;

/**
 * Simple JComboBox based AssetTypeSelector bean implementation.
 */
public class JAssetTypeSelector extends JComboBox implements AssetTypeSelector {

    private static final Logger log = Logger.getLogger("littleware.apps.swingclient.JAssetTypeSelector");
    private final AbstractAssetTypeSelector oselector_support = new AbstractAssetTypeSelector(this);
    private final IconLibrary olib_icon;

    {
        // Update UI when Model changes
        PropertyChangeListener listen_change = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(AssetTypeSelector.Property.selectedAssetType.toString())) {
                    JAssetTypeSelector.this.setSelectedIndex(oselector_support.getSelectedAssetTypeIndex());
                } else {
                    JAssetTypeSelector.this.removeAllItems();
                    JAssetTypeSelector.this.populateCombo();
                    JAssetTypeSelector.this.setSelectedIndex(oselector_support.getSelectedAssetTypeIndex());
                }

            }
        };

        this.addPropertyChangeListener(listen_change);

        // Update Model in response to UI control actions
        this.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt_action) {
                JComboBox wcombo_this = (JComboBox) evt_action.getSource();
                JAssetTypeSelector.this.setSelectedAssetTypeIndex(wcombo_this.getSelectedIndex());
            }
        });

    }

    private void populateCombo() {
        for (AssetType n_type : getAssetTypeOptions()) {
            this.addItem(n_type);
        }
    }

    /**
     * Constructor initializes the component with
     * every available AssetType.
     */
    @Inject
    public JAssetTypeSelector(IconLibrary lib_icon) {
        java.util.List<AssetType> v_options = new ArrayList<AssetType>();
        v_options.addAll(AssetType.getMembers());
        Collections.sort(v_options, new Comparator<AssetType>() {

            @Override
            public int compare(AssetType a, AssetType b) {
                return a.getName().compareTo(b.getName());
            }
        });
        olib_icon = lib_icon;
        oselector_support.setAssetTypeOptions(v_options);

        ListCellRenderer render_custom = new JAssetType(olib_icon);
        this.setRenderer(render_custom);
    }

    /**
     * Constructor initializes the component with a set of AssetTypes
     */
    public JAssetTypeSelector(IconLibrary lib_icon, AssetType[] v_types) {
        olib_icon = lib_icon;
        java.util.List<AssetType> v_options = new ArrayList<AssetType>();
        Collections.addAll(v_options, v_types);
        // listener already registered watching for option registration with underlying model
        oselector_support.setAssetTypeOptions(v_options);

        ListCellRenderer render_custom = new JAssetType(olib_icon);
        this.setRenderer(render_custom);
    }

    @Override
    public java.util.List<AssetType> getAssetTypeOptions() {
        return oselector_support.getAssetTypeOptions();
    }

    @Override
    public void setAssetTypeOptions(java.util.List<AssetType> v_options) {
        oselector_support.setAssetTypeOptions(v_options);
    }

    @Override
    public int getSelectedAssetTypeIndex() {
        return oselector_support.getSelectedAssetTypeIndex();
    }

    @Override
    public void setSelectedAssetTypeIndex(int i_selected) {
        oselector_support.setSelectedAssetTypeIndex(i_selected);
    }

    @Override
    public int getIndexOf(AssetType n_look4) {
        return oselector_support.getIndexOf(n_look4);
    }

    @Override
    public AssetType getSelectedAssetType() {
        return oselector_support.getSelectedAssetType();
    }


    public void addLittleListener(LittleListener listen_action) {
        oselector_support.addLittleListener(listen_action);
    }


    public void removeLittleListener(LittleListener listen_action) {
        oselector_support.removeLittleListener(listen_action);
    }
}
