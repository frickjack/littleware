/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient.wizard;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import com.nexes.wizard.*;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import littleware.apps.client.*;
import littleware.apps.swingclient.*;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.asset.client.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.LittleHome;
import littleware.base.BaseException;
import littleware.base.event.LittleEvent;
import littleware.security.LittleAcl;

/**
 * Specialization of Wizard setup to manage
 * asset creation.
 */
public class CreateAssetWizard extends WizardAssetEditor {

    private static final Logger olog_generic = Logger.getLogger(CreateAssetWizard.class.getName());
    private final Provider<JAssetPathPanel> oprovideAssetPanel;

    /**
     * Panel ids for the initial panels.
     * Type specific info should be added before the 
     * FinalReview.
     */
    public enum BasicPanel {

        PickType,
        PickName,
        PickAcl,
        PickComment,
        PickOwner,
        PickFrom,
        PickTo,
        FinalReview;
    }
    private final IconLibrary olib_icon;
    private final AssetModelLibrary olib_asset;
    private final AssetManager om_asset;
    private final AssetSearchManager om_search;
    private final AssetPathFactory opathFactory;
    private final AssetViewFactory ofactory_view;
    private final ResourceBundle obundle_labels;

    {
        try {
            obundle_labels = ResourceBundle.getBundle("littleware.apps.swingclient.wizard.resources.WizardSupport");
        } catch (RuntimeException e) {
            olog_generic.log(Level.SEVERE, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            throw e;
        }
    }
    private JAssetTypePanel opanel_atype;
    private JTextFieldPanel opanel_name;
    private JAssetPathPanel opanel_acl;
    private JAssetPathPanel opanel_from;
    private JAssetPathPanel opanel_to;
    private JTextFieldPanel opanel_comment;

    /**
     * Internal utility initializes the wizard with the core
     * create-asset wizard panels 
     */
    private void initializePanels() {
        opanel_atype = new JAssetTypePanel(olib_icon);
        opanel_name = new JTextFieldPanel("Enter asset name", 1, 50);
        {
            opanel_acl = oprovideAssetPanel.get();
            opanel_acl.setInstructions("Enter AssetPath to ACL");
            final List<AssetType> vLegal = new ArrayList<AssetType>();
            vLegal.add(LittleAcl.ACL_TYPE);
            opanel_acl.setLegalAssetType(vLegal);
        }
        opanel_from = oprovideAssetPanel.get();
        opanel_from.setInstructions("Enter AssetPath to FROM asset");
        opanel_to = oprovideAssetPanel.get();
        opanel_to.setInstructions("Enter AssetPath to TO asset");
        opanel_comment = new JTextFieldPanel("Enter comment to attach to asset", 3, 60);

        this.registerWizardPanel(BasicPanel.PickType,
                new AssetTypePanelDescriptor(BasicPanel.PickType,
                opanel_atype,
                opanel_atype.getAssetTypeSelector(),
                this) {

                    @Override
                    public BasicPanel getNextPanelDescriptor() {
                        return BasicPanel.PickName;
                    }

                    @Override
                    public void aboutToDisplayPanel() {
                        AssetTypeSelector select_atype = opanel_atype.getAssetTypeSelector();
                        int i_index = select_atype.getIndexOf(getLocalAsset().getAssetType());
                        if (i_index >= 0) {
                            select_atype.setSelectedAssetTypeIndex(i_index);
                        }
                    }

                    @Override
                    public void aboutToHidePanel() {
                        AssetTypeSelector select_atype = opanel_atype.getAssetTypeSelector();
                        if (!getLocalAsset().getAssetType().equals(select_atype.getSelectedAssetType())) {
                            final Asset oldTypeAsset = getLocalAsset();
                            throw new IllegalStateException( "This is busted!" );
                            /*
                            final Asset newTypeAsset = select_atype.getSelectedAssetType().create(
                                    ).name( oldTypeAsset.getName()
                                    ).fromId(oldTypeAsset.getFromId()
                                    ).comment( oldTypeAsset.getComment()
                                    ).aclId(oldTypeAsset.getAclId()
                                    ).build();
                            olib_asset.remove(newTypeAsset.getId());
                            // register bald model with library, so
                            // update events get fired on save
                            setAssetModel(olib_asset.syncAsset(newTypeAsset));
                             *
                             */
                        }
                        if (getLocalAsset().getAssetType().equals(LittleHome.HOME_TYPE)) {
                            changeLocalAsset().setHomeId(getLocalAsset().getId());
                        }
                    }
                });
        this.registerWizardPanel(BasicPanel.PickName,
                new WizardPanelDescriptor(BasicPanel.PickName,
                opanel_name) {

                    @Override
                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickType;
                    }

                    @Override
                    public Object getNextPanelDescriptor() {
                        return BasicPanel.PickAcl;
                    }

                    @Override
                    public void aboutToDisplayPanel() {
                        opanel_name.setText(getLocalAsset().getName());
                    }

                    @Override
                    public void aboutToHidePanel() {
                        String s_old_name = getLocalAsset().getName();
                        String s_new_name = opanel_name.getText();
                        olog_generic.log(Level.FINE, "Hiding name panel, old name: " +
                                s_old_name + ", new name: " + s_new_name);
                        if (!s_old_name.equals(s_new_name)) {
                            changeLocalAsset().setName(opanel_name.getText());
                        }
                    }
                });
        this.registerWizardPanel(BasicPanel.PickAcl,
                new AssetPathPanelDescriptor(BasicPanel.PickAcl,
                opathFactory,
                opanel_acl) {

                    @Override
                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickName;
                    }

                    @Override
                    public Object getNextPanelDescriptor() {
                        return BasicPanel.PickFrom;
                    }

                    @Override
                    public UUID getAssetId() {
                        return getLocalAsset().getAclId();
                    }

                    @Override
                    public void setAssetId(UUID u_asset) {
                        changeLocalAsset().setAclId(u_asset);
                    }
                });
        this.registerWizardPanel(BasicPanel.PickFrom,
                new AssetPathPanelDescriptor(BasicPanel.PickFrom,
                opathFactory,
                opanel_from) {

                    @Override
                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickAcl;
                    }

                    @Override
                    public Object getNextPanelDescriptor() {
                        return BasicPanel.PickTo;
                    }

                    @Override
                    public UUID getAssetId() {
                        return getLocalAsset().getFromId();
                    }

                    @Override
                    public void setAssetId(UUID u_asset) {
                        throw new IllegalStateException( "This is busted!" );
                        /*..
                        changeLocalAsset().setFromId(u_asset);
                        if ((null != u_asset) && (!changeLocalAsset().getAssetType().equals(LittleHome.HOME_TYPE))) {
                            // Must have same HOME when linking FROM an asset
                            try {
                                AssetModel model_from = olib_asset.retrieveAssetModel(u_asset, om_search).get();
                                changeLocalAsset().setHomeId(model_from.getAsset().getHomeId());
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(null,
                                        "Failed to verify HOME id of new FROM asset",
                                        "alert",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                         * 
                         */
                    }
                });
        this.registerWizardPanel(BasicPanel.PickTo,
                new AssetPathPanelDescriptor(BasicPanel.PickTo,
                opathFactory,
                opanel_to) {

                    @Override
                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickFrom;
                    }

                    @Override
                    public Object getNextPanelDescriptor() {
                        return BasicPanel.PickComment;
                    }

                    @Override
                    public UUID getAssetId() {
                        return null; //getLocalAsset().getToId();
                    }

                    @Override
                    public void setAssetId(UUID u_asset) {
                        //changeLocalAsset().setToId(u_asset);
                    }
                });
        this.registerWizardPanel(BasicPanel.PickComment,
                new WizardPanelDescriptor(BasicPanel.PickComment,
                opanel_comment) {

                    @Override
                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickTo;
                    }

                    @Override
                    public Object getNextPanelDescriptor() {
                        return WizardPanelDescriptor.FINISH;
                    }

                    @Override
                    public void aboutToDisplayPanel() {
                        opanel_comment.setText(getLocalAsset().getComment());
                    }

                    @Override
                    public void displayingPanel() {
                        opanel_comment.requestFocus();
                    }

                    @Override
                    public void aboutToHidePanel() {
                        if (!getLocalAsset().getComment().equals(opanel_comment.getText())) {
                            changeLocalAsset().setComment(opanel_comment.getText());
                        }
                    }
                });

        this.setCurrentPanel(BasicPanel.PickType);
    }

    /**
     * Setup a wizard with the various dependencies
     * it needs to setup an asset.
     *
     * @param amodel_start AssetModel referencing a new asset 
     *                        placed into the AssetModelLibrary and
     *                        initialized to whatever extent possible
     */
    @Inject
    public CreateAssetWizard(
            AssetManager m_asset,
            AssetSearchManager m_search,
            AssetModelLibrary lib_asset,
            IconLibrary lib_icon,
            AssetViewFactory factory_view,
            AssetModel amodel_start,
            Provider<JAssetPathPanel> provideAssetPanel,
            AssetPathFactory pathFactory) {
        super(m_asset, m_search, lib_asset, lib_icon);

        oprovideAssetPanel = provideAssetPanel;
        olib_icon = lib_icon;
        olib_asset = lib_asset;
        om_search = m_search;
        om_asset = m_asset;
        ofactory_view = factory_view;
        opathFactory = pathFactory;

        setAssetModel(amodel_start);
        initializePanels();
    }

    /**
     * Get the set of legal asset-types to restrict the user selection to.
     * Empty implies no restrictions on type.
     * Listed in order of display.
     */
    public List<AssetType> getLegalAssetType() {
        return opanel_atype.getAssetTypeSelector().getAssetTypeOptions();
    }

    public void setLegalAssetType(List<AssetType> v_legal) {
        opanel_atype.getAssetTypeSelector().setAssetTypeOptions(v_legal);
    }

    /**
     * Create-asset wizard always has local changes!
     */
    @Override
    public boolean getHasLocalChanges() {
        return true;
    }

    @Override
    protected void eventFromModel(LittleEvent evt_model) {
    }

    /** Verify that the comment from the last panel is set */
    @Override
    public void saveLocalChanges(AssetManager m_asset, String s_message) throws BaseException, AssetException,
            RemoteException, GeneralSecurityException {
        if (!getLocalAsset().getComment().equals(opanel_comment.getText())) {
            changeLocalAsset().setComment(opanel_comment.getText());
        }
        super.saveLocalChanges(m_asset, s_message);
    }
}
