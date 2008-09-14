package littleware.apps.swingclient.wizard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import javax.swing.*;
import javax.swing.event.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.UUID;

import com.nexes.wizard.*;

import littleware.apps.client.*;
import littleware.apps.swingclient.*;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.AssetManager;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.InvalidAssetTypeException;
import littleware.base.BaseException;
import littleware.base.FactoryException;
import littleware.base.AssertionFailedException;
import littleware.security.SecurityAssetType;

/**
 * Specialization of Wizard setup to manage
 * asset creation.
 */
public class CreateAssetWizard extends WizardAssetEditor {

    private static final Logger olog_generic = Logger.getLogger("littleware.apps.swingclient.wizard.CreateAssetWizard ");

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

    
    
    private final  IconLibrary olib_icon;
    private final  AssetModelLibrary olib_asset;
    private final  AssetManager om_asset;
    private final  AssetSearchManager om_search  ;
    private final  AssetPathFactory ofactory_path;       
    private final   AssetViewFactory     ofactory_view;
    private final   ResourceBundle       obundle_labels;
    {
        try {
            ofactory_path = AssetPathFactory.getFactory();
            obundle_labels = ResourceBundle.getBundle ( "littleware.apps.swingclient.wizard.resources.WizardSupport");
        } catch ( RuntimeException e ) {
            olog_generic.log ( Level.SEVERE, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace( e ) 
                    );
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
     *
     * @param v_types to restrict asset-type panel to - may be null
     */
    private void initializePanels(List<AssetType> v_types) {
        opanel_atype = new JAssetTypePanel(olib_icon);
        opanel_name = new JTextFieldPanel("Enter asset name", 1, 50);
        {
            Set<AssetType> v_acltype = new HashSet<AssetType>();
            v_acltype.add(SecurityAssetType.ACL);
            opanel_acl = new JAssetPathPanel("Enter AssetPath to ACL", v_acltype,
                    om_search, olib_asset, olib_icon,
                    ofactory_view, this);
        }
        opanel_from = new JAssetPathPanel("Enter AssetPath to FROM asset", null,
                om_search, olib_asset, olib_icon,
                ofactory_view, this);
        opanel_to = new JAssetPathPanel("Enter AssetPath to TO asset", null,
                om_search, olib_asset, olib_icon,
                ofactory_view, this);
        opanel_comment = new JTextFieldPanel("Enter comment to attach to asset", 3, 60);
        if (null != v_types) {
            opanel_atype.getAssetTypeSelector().setAssetTypeOptions(v_types);
        }

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
                            Asset a_old = getLocalAsset();
                            Asset a_new = select_atype.getSelectedAssetType().create();
                            a_new.setFromId(a_old.getFromId());
                            a_new.setAclId(a_old.getAclId());
                            a_new.setToId(a_old.getToId());
                            a_new.setComment(a_old.getComment());
                            a_new.setObjectId(a_old.getObjectId());
                            a_new.setName(a_old.getName());
                            setAssetModel(olib_asset.syncAsset(a_new));
                            setHasLocalChanges(true);
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
                opanel_acl) {

                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickName;
                    }

                    public Object getNextPanelDescriptor() {
                        return BasicPanel.PickFrom;
                    }

                    public UUID getAssetId() {
                        return getLocalAsset().getAclId();
                    }

                    public void setAssetId(UUID u_asset) {
                        changeLocalAsset().setAclId(u_asset);
                    }
                });
        this.registerWizardPanel(BasicPanel.PickFrom,
                new AssetPathPanelDescriptor(BasicPanel.PickFrom,
                opanel_from) {

                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickAcl;
                    }

                    public Object getNextPanelDescriptor() {
                        return BasicPanel.PickTo;
                    }

                    public UUID getAssetId() {
                        return getLocalAsset().getFromId();
                    }

                    public void setAssetId(UUID u_asset) {
                        changeLocalAsset().setFromId(u_asset);
                        if (null != u_asset) {
                            // Must have same HOME when linking FROM an asset
                            try {
                                AssetModel model_from = olib_asset.retrieveAssetModel(u_asset, om_search);
                                changeLocalAsset().setHomeId(model_from.getAsset().getHomeId());
                            } catch (Exception e) {
                                JOptionPane.showMessageDialog(null,
                                        "Failed to verify HOME id of new FROM asset",
                                        "alert",
                                        JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    }
                });
        this.registerWizardPanel(BasicPanel.PickTo,
                new AssetPathPanelDescriptor(BasicPanel.PickTo,
                opanel_to) {

                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickFrom;
                    }

                    public Object getNextPanelDescriptor() {
                        return BasicPanel.PickComment;
                    }

                    public UUID getAssetId() {
                        return getLocalAsset().getToId();
                    }

                    public void setAssetId(UUID u_asset) {
                        changeLocalAsset().setToId(u_asset);
                    }
                });
        this.registerWizardPanel(BasicPanel.PickComment,
                new WizardPanelDescriptor(BasicPanel.PickComment,
                opanel_comment) {

                    public BasicPanel getBackPanelDescriptor() {
                        return BasicPanel.PickTo;
                    }

                    public Object getNextPanelDescriptor() {
                        return WizardPanelDescriptor.FINISH;
                    }

                    public void aboutToDisplayPanel() {
                        opanel_comment.setText(getLocalAsset().getComment());
                    }

                    public void displayingPanel() {
                        opanel_comment.requestFocus();
                    }

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
     * @param v_types list of AssetTypes to restrict
     *             the new asset to - may be null
     * @param amodel_start AssetModel referencing a new asset 
     *                        placed into the AssetModelLibrary and
     *                        initialized to whatever extent possible
     */
    public CreateAssetWizard(
            AssetManager m_asset,
            AssetSearchManager m_search,
            AssetModelLibrary lib_asset,
            IconLibrary lib_icon,
            AssetViewFactory factory_view,
            List<AssetType> v_types,
            AssetModel amodel_start) {
        super(m_asset, m_search, lib_asset, lib_icon);

        olib_icon = lib_icon;
        olib_asset = lib_asset;
        om_search = m_search;
        om_asset = m_asset;
        ofactory_view = factory_view;

        setAssetModel(amodel_start);
        initializePanels(v_types);
    }

    /**
     * Setup a wizard parented on the given frame and with the given dependencies
     *
     * @param wframe_owner that owns the wizard&apos;s JDialog
     * @param v_types list of AssetTypes to restrict
     *             the new asset to - may be null 
     * @param amodel_start AssetModel referencing a new asset 
     *                        placed into the AssetModelLibrary and
     *                        initialized to whatever extent possible     
     */
    public CreateAssetWizard(JFrame wframe_owner,
            AssetManager m_asset,
            AssetSearchManager m_search,
            AssetModelLibrary lib_asset,
            IconLibrary lib_icon,
            AssetViewFactory factory_view,
            List<AssetType> v_types,
            AssetModel amodel_start) {
        super(wframe_owner, m_asset, m_search, lib_asset, lib_icon);
        olib_icon = lib_icon;
        olib_asset = lib_asset;
        om_search = m_search;
        om_asset = m_asset;
        ofactory_view = factory_view;

        setAssetModel(amodel_start);
        initializePanels(v_types);
    }

    protected void eventFromModel(LittleEvent evt_model) {
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

