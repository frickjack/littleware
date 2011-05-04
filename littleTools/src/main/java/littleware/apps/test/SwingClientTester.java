/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.test;

import littleware.apps.swingclient.AbstractAssetEditor;
import littleware.apps.swingclient.AssetView;
import littleware.apps.swingclient.AssetViewFactory;
import littleware.apps.swingclient.AssetEditorFactory;
import littleware.apps.swingclient.AssetEditor;
import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetManager;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.security.LittleGroup.Builder;
import littleware.test.JLittleDialog;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.*;

import com.nexes.wizard.Wizard;
import java.util.ArrayList;
import java.util.List;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.*;
import littleware.apps.swingclient.wizard.CreateAssetWizard;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleTool;
import littleware.security.auth.*;
import littleware.security.*;

/**
 * Tester for the SimpleXmlDataAsset Asset super class handling
 * of XML-based getData/setData via annotations.
 */
public class SwingClientTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(SwingClientTester.class.getName());
    private final IconLibrary olib_icon;
    private final AssetLibrary assetLibrary;
    private final AssetViewFactory ofactory_view;
    private final AssetEditorFactory ofactory_edit;
    private final Provider<CreateAssetWizard> oprovideWizard;
    private final Provider<JAssetBrowser> oprovideBrowser;
    private final Provider<ExtendedAssetViewController> oprovideController;
    private final Provider<JSimpleAssetToolbar> oprovideToolbar;
    private final AssetSearchManager search;
    private final LittleSession session;
    private final AssetManager saver;
    private final Provider<TreeNodeBuilder> nodeProvider;
    private final Provider<GenericBuilder> genericProvider;
    private final Provider<Builder> groupProvider;
    private final Provider<LittleAclEntry.Builder> aclEntryProvider;
    private final Provider<LittleAcl.Builder> aclProvider;

    /** Inject dependencies */
    @Inject
    public SwingClientTester(AssetSearchManager search,
            LittleSession session,
            AssetManager saver,
            AssetLibrary lib_asset, IconLibrary lib_icon,
            AssetViewFactory factory_view,
            AssetEditorFactory factory_edit,
            Provider<CreateAssetWizard> provideWizard,
            Provider<JAssetBrowser> provideBrowser,
            Provider<ExtendedAssetViewController> provideController,
            Provider<JSimpleAssetToolbar> provideToolbar,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            Provider<GenericAsset.GenericBuilder> genericProvider,
            Provider<LittleGroup.Builder> groupProvider,
            Provider<LittleAcl.Builder> aclProvider,
            Provider<LittleAclEntry.Builder> aclEntryProvider
        ) {
        this.search = search;
        this.session = session;
        this.saver = saver;
        this.nodeProvider = nodeProvider;
        this.genericProvider = genericProvider;
        this.groupProvider = groupProvider;
        this.aclProvider = aclProvider;
        this.aclEntryProvider = aclEntryProvider;
        assetLibrary = lib_asset;
        olib_icon = lib_icon;
        ofactory_view = factory_view;
        ofactory_edit = factory_edit;
        oprovideWizard = provideWizard;
        oprovideBrowser = provideBrowser;
        oprovideController = provideController;
        oprovideToolbar = provideToolbar;
    }

    public void testClientSession() {
        try {
            // assert that the login session has a non-zero duration
            assertTrue("Session t_end (" + session.getEndDate() + ") > t_start ("
                    + session.getCreateDate() + ")",
                    session.getEndDate().getTime() > (session.getCreateDate().getTime() + 60 * 1000));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            assertTrue("Caught unexpected: " + ex, false);
        }
    }

    /**
     * Run the injected AssetLibrary through a few simple tests
     */
    public void testAssetModelLibrary() {
        final List<Asset> cleanupList = new ArrayList<Asset>();

        try {
            final LittleHome testHome = getTestHome(search);
            final GenericAsset testAsset1 = genericProvider.get().name("bogus1").build();
            final TreeNode testAsset2 = nodeProvider.get().name("bogus2").build();
            cleanupList.add(testAsset1);
            cleanupList.add(testAsset2);

            assetLibrary.remove(session.getId());

            assertTrue("Simple sync is ok",
                    assetLibrary.syncAsset(session).getAsset() == session);
            assertTrue("No retrieval if not necessary",
                    assetLibrary.retrieveAssetModel(session.getId(), search).get().getAsset() == session);

            final AssetRef amodel_everybody =
                    assetLibrary.syncAsset(search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleGroup.GROUP_TYPE).get());
            assertTrue("ModelLibrary getByName inheritance aware 1",
                    assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittlePrincipal.PRINCIPAL_TYPE) != null);
            assertTrue("ModelLibrary getByName inheritance aware 2",
                    assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleUser.USER_TYPE) == null);
            assertTrue("ModelLibrary getByName inheritance aware 3",
                    assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleGroup.GROUP_TYPE) != null);
            assetLibrary.remove(amodel_everybody.getAsset().getId());
            assertTrue("ModelLibrary getByName cleared after remove",
                    assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleGroup.GROUP_TYPE) == null);

            final AssetEditor edit_bogus = new AbstractAssetEditor(this) {

                @Override
                public void eventFromModel(LittleEvent event_property) {
                    // just do something - anything
                    log.log(Level.INFO, "Test editor received event from model, setting value to 5");
                    changeLocalAsset().narrow(GenericAsset.GenericBuilder.class).setValue(5);
                }
            };
            edit_bogus.setAssetModel(assetLibrary.syncAsset(testAsset1)); //addPropertyChangeListener ( listen_assetprop );
            // Adding a_bogus2 to the asset repository should trigger a Property.assetsLinkingFrom
            // property-change event on listeners to a_bogus1 AssetRef
            assetLibrary.syncAsset(testAsset2.copy().parentId(testAsset1.getId()).build());
            Thread.sleep(4000); // let any asynchrony work itself out
            assertTrue("AssetModel cascading properties correctly", 5 == edit_bogus.getLocalAsset().narrow(GenericAsset.class).getValue());

        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            fail("Caught unexpected: " + ex);
        } finally {
            for (Asset trash : cleanupList) {
                assetLibrary.remove(trash.getId());
            }
        }
    }

    /**
     * Popup a JGenericAssetView for the tester to verify
     */
    public void testJAssetViews() {
        try {
            AssetRef model_asset = assetLibrary.syncAsset(session);
            JComponent w_asset = (JComponent) ofactory_view.createView(model_asset);

            //w_asset.setPreferredSize ( new Dimension ( 800, 700 ) );  // force big
            assertTrue("User confirmed asset-viewer UI functional",
                    JLittleDialog.showTestDialog(w_asset,
                    "play with the AssetView widget. \n"
                    + "Hit OK when test successfully done"));

            log.log(Level.INFO, "GETTING GROUP: " + AccountManager.LITTLEWARE_EVERYBODY_GROUP);
            final LittleGroup group_everybody = search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleGroup.GROUP_TYPE).get().narrow();
            AssetRef model_everybody = assetLibrary.syncAsset(group_everybody);
            log.log(Level.INFO, "GROUP SYNCED: " + AccountManager.LITTLEWARE_EVERYBODY_GROUP);

            JComponent wview_group = (JComponent) ofactory_view.createView(model_everybody);
            wview_group.setPreferredSize(new Dimension(800, 700));  // force big

            assertTrue("User confirmed group-viewer UI functional",
                    JLittleDialog.showTestDialog(wview_group,
                    "play with the GroupView widget. \n"
                    + "Hit OK when test successfully done"));

            final LittleAcl acl_everybody = search.getByName(LittleAcl.ACL_EVERYBODY_READ,
                    LittleAcl.ACL_TYPE).get().narrow();
            JComponent w_acl = (JComponent) ofactory_view.createView(assetLibrary.syncAsset(acl_everybody));
            //w_acl.setPreferredSize ( new Dimension ( 800, 700 ) );  // force big
            assertTrue("User confirmed acl-viewer UI functional",
                    JLittleDialog.showTestDialog(w_acl,
                    "play with the AclView widget. \n"
                    + "Hit OK when test successfully done"));
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed test", e);
            fail("Caught: " + e);
        }
    }

    /**
     * Test the JAssetBrowser
     */
    public void testJAssetBrowser() {
        try {
            AssetRef model_asset = assetLibrary.syncAsset(session);
            JComponent wbrowser_asset = oprovideBrowser.get();
            SimpleAssetViewController listen_control = oprovideController.get();
            listen_control.setControlView((AssetView) wbrowser_asset);
            final JSimpleAssetToolbar wtoolbar_asset = oprovideToolbar.get();
            wtoolbar_asset.setConnectedView((AssetView) wbrowser_asset);
            wtoolbar_asset.getButton(JSimpleAssetToolbar.ButtonId.CREATE).setEnabled(true);
            wtoolbar_asset.getButton(JSimpleAssetToolbar.ButtonId.EDIT).setEnabled(true);
            wtoolbar_asset.getButton(JSimpleAssetToolbar.ButtonId.DELETE).setEnabled(true);

            ((LittleTool) wtoolbar_asset).addLittleListener(listen_control);

            ((AssetView) wbrowser_asset).setAssetModel(model_asset);

            final JPanel wpanel_browser = new JPanel(new BorderLayout());
            //wpanel_browser.setLayout( new BoxLayout( wpanel_browser, BoxLayout.Y_AXIS ) );
            wpanel_browser.add(wtoolbar_asset, BorderLayout.NORTH);
            wpanel_browser.add(wbrowser_asset, BorderLayout.CENTER);
            //wpanel_browser.setPreferredSize ( new Dimension ( 1200, 700 ) );  // force big

            /*.......
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            JFrame   w_frame = new JFrame ();
            w_frame.getContentPane ().add ( wpanel_browser );
            w_frame.pack ();
            w_frame.setVisible ( true );                    
            }
            });
            .......*/

            assertTrue("User confirmed browser UI functional",
                    JLittleDialog.showTestDialog(wpanel_browser, //new JLabel ( "Play with the browser" ),
                    "play with the asset browser widget. \n"
                    + "Hit OK when test successfully done"));

        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            fail("Caught: " + ex);
        }
    }

    /**
     * Test some editors
     */
    public void testJEditor() {
        try {
            final LittleHome testHome = getTestHome( search );
            assetLibrary.syncAsset(session);

            if (true) { // Simple group editor
                final LittleGroup group_everybody = search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                        LittleGroup.GROUP_TYPE).get().narrow();
                LittleGroup group_test = (LittleGroup) search.getByName("group.littleware.test_user",
                        LittleGroup.GROUP_TYPE).getOr(null);
                if (null == group_test) {
                    group_test = saver.saveAsset(groupProvider.get().name("group.littleware.test_user").parent(getTestHome(search)).build(),
                            "Setup test asset").narrow();
                }
                group_test = group_test.copy().add(group_everybody).build();

                assertTrue("Test group " + group_test.getName() + " has members",
                        !group_test.getMembers().isEmpty());
                AssetRef model_test = assetLibrary.syncAsset(group_test);
                JComponent wedit_group = (JComponent) ofactory_edit.createView(model_test);
                //wedit_group.setPreferredSize ( new Dimension ( 1000, 700 ) );  // force big

                assertTrue("User confirmed group-editor UI functional",
                        JLittleDialog.showTestDialog(wedit_group,
                        "play with the GroupEditor widget. \n"
                        + "Hit OK when test successfully done"));
            }
            if (true) { // Simple acl editor
                final String s_acl_name = "SwingClientTester.acl";
                LittleAcl acl_test = (LittleAcl) search.getByName(s_acl_name,
                        LittleAcl.ACL_TYPE).getOr(null);
                if (null == acl_test) {
                    final Asset a_testhome = search.getByName("littleware.test_home", LittleHome.HOME_TYPE).get();
                    final LittleGroup groupEverbody = search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP, LittleGroup.GROUP_TYPE).get().narrow();
                    final LittleAcl acl = aclProvider.get().name(s_acl_name).parent(testHome).
                            comment("AclEditor test acl").build().narrow();

                    acl_test = saver.saveAsset(acl.copy().addEntry(
                            aclEntryProvider.get().
                            principal(groupEverbody).
                            acl(acl).
                            addPermission(LittlePermission.READ).
                            build()).aclId(acl.getId()).build(), "Setting up test asset").narrow();
                }
                AssetRef model_test = assetLibrary.syncAsset(acl_test);
                JComponent wedit_acl = (JComponent) ofactory_edit.createView(model_test); // Add AclEditor to Factory!!!
                //wedit_acl.setPreferredSize ( new Dimension ( 1000, 700 ) );  // force big

                assertTrue("User confirmed acl-editor UI functional",
                        JLittleDialog.showTestDialog(wedit_acl,
                        "play with the AclEditor widget. \n"
                        + "Hit OK when test successfully done"));
            }

        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            fail("Caught: " + ex);
        }
    }

    /**
     * Run the create-asset wizard through a test
     */
    public void testWizardCreate() {
        final List<Asset> cleanupList = new ArrayList<Asset>();

        try {
            final LittleHome testHome = getTestHome(search);
            final GenericAsset testAsset = genericProvider.get().name("test_asset").
                    parent(testHome).build();
            cleanupList.add(testAsset);
            final CreateAssetWizard wizard_create = oprovideWizard.get();
            wizard_create.setAssetModel(assetLibrary.syncAsset(testAsset));
            assertTrue("User closed create-asset wizard ok",
                    Wizard.FINISH_RETURN_CODE == wizard_create.showModalDialog());
            assertTrue("Asset-create wizard has asset changes",
                    wizard_create.getHasLocalChanges());
        } catch (Exception ex) {
            log.log(Level.INFO, "Failed test", ex);
            fail("Caught unexpected: " + ex);
        } finally {
            for( Asset trash : cleanupList ) {
                assetLibrary.remove( trash.getId() );
            }
        }
    }
    /**
     * Popup the JGroupsUnderParent tool
     *
    public void testGroupFolderTool() {
    try {
    final Factory<UUID> factory_uuid = UUIDFactory.getFactory();
    final AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
    final AssetManager m_asset = om_helper.getService(ServiceType.ASSET_MANAGER);

    final LittleAcl acl_everybody =  m_search.getByName(LittleAcl.ACL_EVERYBODY_READ,
    LittleAcl.ACL_TYPE);
    olib_asset.syncAsset(acl_everybody);
    final Map<String, UUID> v_homeids = m_search.getHomeAssetIds();
    final UUID u_home = v_homeids.get("littleware.test_home");
    final String s_foldername = "testGroupFolderTool";
    Asset a_folder = m_search.getAssetFromOrNull(u_home, s_foldername);

    if (null == a_folder) {
    final LittleGroup group_everybody =  m_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
    LittleGroup.GROUP_TYPE);

    a_folder = GenericAsset.GENERIC.create();
    a_folder.setHomeId(u_home);
    a_folder.setAclId(acl_everybody.getId());
    a_folder.setFromId(u_home);
    a_folder.setName(s_foldername);
    a_folder = m_asset.saveAsset(a_folder, "Setting up test folder");

    LittleGroup group_copy = LittleGroup.GROUP_TYPE.create();
    group_copy.sync(group_everybody);
    group_copy.setHomeId(u_home);
    group_copy.setName(s_foldername + ".copy1");
    group_copy.setId(factory_uuid.create());
    group_copy.setFromId(a_folder.getId());
    group_copy.setAclId(acl_everybody.getId());
    group_copy.setOwnerId(a_folder.getOwnerId());
    m_asset.saveAsset(group_copy, "Setting up test GROUP 1");

    group_copy.setId(factory_uuid.create());
    group_copy.setName(s_foldername + ".copy2");
    m_asset.saveAsset(group_copy, "Setup up test GROUP 2");
    }

    GroupFolderTool grouptool_test = GroupFolderTool.create(olib_asset.syncAsset(a_folder),
    m_asset,
    m_search,
    ofactory_view,
    olib_icon);
    JPanel wpanel_test = new JPanel(new GridBagLayout());
    GridBagConstraints gcontrol_panel = new GridBagConstraints();
    gcontrol_panel.gridx = 0;
    gcontrol_panel.gridy = 0;
    gcontrol_panel.anchor = GridBagConstraints.LINE_START;
    gcontrol_panel.gridwidth = 1;
    gcontrol_panel.gridheight = 1;
    //gcontrol_panel.fill = GridBagConstraints.HORIZONTAL;

    wpanel_test.add(grouptool_test.getToolbar(), gcontrol_panel);
    gcontrol_panel.weighty = 0.5;
    gcontrol_panel.fill = GridBagConstraints.BOTH;
    gcontrol_panel.gridwidth = GridBagConstraints.REMAINDER;
    gcontrol_panel.gridy += gcontrol_panel.gridheight;

    wpanel_test.add(grouptool_test.getGroupsView(), gcontrol_panel);
    wpanel_test.setPreferredSize(new Dimension(1000, 700));  // force big

    assertTrue("User confirmed group-editor UI functional",
    JLittleDialog.showTestDialog(wpanel_test,
    "play with the GroupsUnderParent tool. \n" +
    "Hit OK when test successfully done"));
    } catch (Exception e) {
    olog_generic.log(Level.WARNING, "Caught unexpected: " + e + ", " +
    BaseException.getStackTrace(e));
    assertTrue("Caught unexpected: " + e, false);
    }
    }
     */
}
