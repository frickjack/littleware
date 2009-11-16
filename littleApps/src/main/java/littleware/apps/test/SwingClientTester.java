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

import littleware.base.feedback.LittleListener;
import littleware.base.feedback.LittleTool;
import littleware.base.feedback.LittleEvent;
import littleware.test.JLittleDialog;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.*;

import com.nexes.wizard.Wizard;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.*;
import littleware.apps.swingclient.wizard.CreateAssetWizard;
import littleware.base.*;
import littleware.security.auth.*;
import littleware.security.*;
import littleware.test.LittleTest;

/**
 * Tester for the SimpleXmlDataAsset Asset super class handling
 * of XML-based getData/setData via annotations.
 */
public class SwingClientTester extends LittleTest {

    private static final Logger olog_generic = Logger.getLogger(SwingClientTester.class.getName());
    private final IconLibrary olib_icon;
    private final SessionHelper om_helper;
    private final AssetModelLibrary olib_asset;
    private final AssetViewFactory ofactory_view;
    private final AssetEditorFactory ofactory_edit;
    private final Provider<CreateAssetWizard> oprovideWizard;
    private final Provider<JAssetBrowser> oprovideBrowser;
    private final Provider<ExtendedAssetViewController> oprovideController;
    private final Provider<JSimpleAssetToolbar> oprovideToolbar;

    
    /** Inject dependencies */
    @Inject
    public SwingClientTester( SessionHelper m_helper,
        AssetModelLibrary lib_asset, IconLibrary lib_icon,
        AssetViewFactory factory_view,
        AssetEditorFactory factory_edit,
        Provider<CreateAssetWizard> provideWizard,
        Provider<JAssetBrowser> provideBrowser,
        Provider<ExtendedAssetViewController>  provideController,
        Provider<JSimpleAssetToolbar>  provideToolbar
        ) {
        om_helper = m_helper;
        olib_asset = lib_asset;
        olib_icon = lib_icon;
        ofactory_view = factory_view;
        ofactory_edit = factory_edit;
        oprovideWizard = provideWizard;
        oprovideBrowser = provideBrowser;
        oprovideController = provideController;
        oprovideToolbar = provideToolbar;
    }

    /** 
     * No setup necessary
     */
    @Override
    public void setUp() {
    }

    /** Do nothing	 */
    @Override
    public void tearDown() {

    }

    public void testClientSession () {
        try {
            // assert that the login session has a non-zero duration
            LittleSession session = om_helper.getSession();
            assertTrue( "Session t_end (" + session.getEndDate() + ") > t_start (" +
                    session.getStartDate() + ")",
                    session.getEndDate().getTime() > (session.getStartDate().getTime () + 60 * 1000)
                    );
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        }
    }

    /**
     * Run the injected AssetModelLibrary through a few simple tests
     */
    public void testAssetModelLibrary() {
        // couple bogus test assets - donot save to repository
        final Asset a_bogus1 = AssetType.GENERIC.create().name("bogus1").build();
        final Asset a_bogus2 = AssetType.GENERIC.create().name("bogus2").build();


        try {
            final Asset a_test = om_helper.getSession();
            final AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);

            olib_asset.remove(a_test.getId());

            assertTrue("Simple sync is ok",
                    olib_asset.syncAsset(a_test).getAsset() == a_test);
            assertTrue("No retrieval if not necessary",
                    olib_asset.retrieveAssetModel(a_test.getId(), m_search).get().getAsset() == a_test
                    );
            
            AssetModel amodel_everybody =
                    olib_asset.syncAsset( m_search.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP, 
                    SecurityAssetType.GROUP ).get()
                    );
            assertTrue( "ModelLibrary getByName inheritance aware 1",
                    olib_asset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.PRINCIPAL
                    ) != null
                    );
            assertTrue( "ModelLibrary getByName inheritance aware 2",
                    olib_asset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.USER
                    ) == null
                    );
            assertTrue( "ModelLibrary getByName inheritance aware 3",
                    olib_asset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP
                    ) != null
                    );
            olib_asset.remove( amodel_everybody.getAsset ().getId () );
            assertTrue( "ModelLibrary getByName cleared after remove",
                    olib_asset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP
                    ) == null
                    );

            final AssetEditor edit_bogus = new AbstractAssetEditor(this) {

                @Override
                public void eventFromModel(LittleEvent event_property) {
                    // just do something - anything
                    olog_generic.log( Level.INFO, "Test editor received event from model, setting value to 5" );
                    changeLocalAsset().setValue(5);
                }
            };
            edit_bogus.setAssetModel(olib_asset.syncAsset(a_bogus1)); //addPropertyChangeListener ( listen_assetprop );
            // Adding a_bogus2 to the asset repository should trigger a Property.assetsLinkingFrom
            // property-change event on listeners to a_bogus1 AssetModel
            olib_asset.syncAsset(a_bogus2.copy().fromId(a_bogus1.getId()).build());
            Thread.sleep(4000); // let any asynchrony work itself out
            assertTrue("AssetModel cascading properties correctly", 5 == edit_bogus.getLocalAsset().getValue());

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            olib_asset.remove(a_bogus1.getId());
            olib_asset.remove(a_bogus2.getId());
        }
    }

    /**
     * Popup a JSessionManager for the tester to verify
     */
    public void testJSessionManager() {
        try {
            // Force RMI
            SessionUtil util = SessionUtil.get ();
            SessionManager m_session = util.getSessionManager( util.getRegistryHost (), util.getRegistryPort () );

            final JSessionManager wm_session = new JSessionManager(m_session);
            wm_session.addLittleListener(
                    new LittleListener() {

                @Override
                        public void receiveLittleEvent(LittleEvent event_little) {
                            String s_session_info = null;
                            if (event_little.isSuccessful()) {
                                try {
                                    UUID u_session = ((SessionHelper) event_little.getResult()).getSession().getId();
                                    s_session_info = "session id: " + u_session.toString();
                                } catch (Exception e) {
                                    s_session_info = "ERROR loading session: caught unexpected: " + e;
                                }
                            }
                            JOptionPane.showConfirmDialog(wm_session, event_little.getOperation() +
                                    " was successful ? " + event_little.isSuccessful() +
                                    ", " + s_session_info);
                        }
                    });
            assertTrue("User confirmed JSessionManager UI functional",
                    JLittleDialog.showTestDialog(wm_session,
                    "login a few times, and verify result"));
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }


    /**
     * Popup a JGenericAssetView for the tester to verify
     */
    public void testJAssetViews() {
        try {
            AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
            AssetModel model_asset = olib_asset.syncAsset(om_helper.getSession());
            JComponent w_asset = (JComponent) ofactory_view.createView(model_asset);

            //w_asset.setPreferredSize ( new Dimension ( 800, 700 ) );  // force big
            assertTrue("User confirmed asset-viewer UI functional",
                    JLittleDialog.showTestDialog(w_asset,
                    "play with the AssetView widget. \n" +
                    "Hit OK when test successfully done"));

            olog_generic.log(Level.INFO, "GETTING GROUP: " + AccountManager.LITTLEWARE_EVERYBODY_GROUP);
            final LittleGroup group_everybody = m_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP).get().narrow();
            AssetModel model_everybody = olib_asset.syncAsset(group_everybody);
            olog_generic.log(Level.INFO, "GROUP SYNCED: " + AccountManager.LITTLEWARE_EVERYBODY_GROUP);

            JComponent wview_group = (JComponent) ofactory_view.createView(model_everybody);
            wview_group.setPreferredSize(new Dimension(800, 700));  // force big

            assertTrue("User confirmed group-viewer UI functional",
                    JLittleDialog.showTestDialog(wview_group,
                    "play with the GroupView widget. \n" +
                    "Hit OK when test successfully done"));

            final LittleAcl acl_everybody = m_search.getByName(LittleAcl.ACL_EVERYBODY_READ,
                    SecurityAssetType.ACL).get().narrow();
            JComponent w_acl = (JComponent) ofactory_view.createView(olib_asset.syncAsset(acl_everybody));
            //w_acl.setPreferredSize ( new Dimension ( 800, 700 ) );  // force big
            assertTrue("User confirmed acl-viewer UI functional",
                    JLittleDialog.showTestDialog(w_acl,
                    "play with the AclView widget. \n" +
                    "Hit OK when test successfully done"));
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }

    /**
     * Test the JAssetBrowser
     */
    public void testJAssetBrowser() {
        try {
            AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
            AssetModel model_asset = olib_asset.syncAsset(om_helper.getSession());
            JComponent wbrowser_asset = oprovideBrowser.get();
            SimpleAssetViewController listen_control = oprovideController.get();
            listen_control.setControlView((AssetView) wbrowser_asset);
            final JSimpleAssetToolbar wtoolbar_asset = oprovideToolbar.get();
            wtoolbar_asset.setConnectedView( (AssetView) wbrowser_asset );
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
                    "play with the asset browser widget. \n" +
                    "Hit OK when test successfully done"));

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }

    /**
     * Test some editors
     */
    public void testJEditor() {
        try {
            final AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);

            olib_asset.syncAsset(om_helper.getSession());

            if (true) { // Simple group editor
                final LittleGroup group_everybody = m_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                        SecurityAssetType.GROUP).get().narrow();
                LittleGroup group_test = (LittleGroup) m_search.getByName("group.littleware.test_user",
                                                        SecurityAssetType.GROUP
                                                        ).getOr(null);
                if ( null == group_test ) {
                    final AssetManager saver = om_helper.getService( ServiceType.ASSET_MANAGER );
                    group_test = saver.saveAsset( SecurityAssetType.GROUP.create().name( "group.littleware.test_user" ).parent( getTestHome( m_search) ).build(),
                            "Setup test asset"
                            ).narrow();
                }
                group_test.addMember(group_everybody);

                assertTrue("Test group " + group_test.getName() + " has members",
                        group_test.members().hasMoreElements());
                AssetModel model_test = olib_asset.syncAsset(group_test);
                JComponent wedit_group = (JComponent) ofactory_edit.createView(model_test);
                //wedit_group.setPreferredSize ( new Dimension ( 1000, 700 ) );  // force big

                assertTrue("User confirmed group-editor UI functional",
                        JLittleDialog.showTestDialog(wedit_group,
                        "play with the GroupEditor widget. \n" +
                        "Hit OK when test successfully done"));
            }
            if (true) { // Simple acl editor
                final String s_acl_name = "SwingClientTester.acl";
                LittleAcl acl_test = (LittleAcl) m_search.getByName(s_acl_name,
                        SecurityAssetType.ACL).getOr(null);
                if (null == acl_test) {
                    final Asset a_testhome = m_search.getByName("littleware.test_home", AssetType.HOME).get();
                    final LittleGroup groupEverbody = m_search.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP, SecurityAssetType.GROUP ).get().narrow();
                    final LittleAcl.Builder builder = (LittleAcl.Builder) SecurityAssetType.ACL.create().name( s_acl_name ).parent( a_testhome).
                                    comment("AclEditor test acl");
                    builder.setAclId( builder.getId() );
                    builder.addEntry(
                        SecurityAssetType.ACL_ENTRY.create().
                                            principal(groupEverbody).
                                            addPermission( LittlePermission.READ ).
                                            build()
                                            );

                    acl_test = om_helper.getService(ServiceType.ASSET_MANAGER).saveAsset(builder.build(), "Setting up test asset");
                }
                AssetModel model_test = olib_asset.syncAsset(acl_test);
                JComponent wedit_acl = (JComponent) ofactory_edit.createView(model_test); // Add AclEditor to Factory!!!
                //wedit_acl.setPreferredSize ( new Dimension ( 1000, 700 ) );  // force big

                assertTrue("User confirmed acl-editor UI functional",
                        JLittleDialog.showTestDialog(wedit_acl,
                        "play with the AclEditor widget. \n" +
                        "Hit OK when test successfully done"));
            }

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }

    /**
     * Run the create-asset wizard through a test
     */
    public void testWizardCreate() {
        Asset a_new = AssetType.GENERIC.create().name("test_asset").build();

        try {
            AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
            final CreateAssetWizard wizard_create = oprovideWizard.get();
            wizard_create.setAssetModel( olib_asset.syncAsset(a_new));
            assertTrue("User closed create-asset wizard ok",
                    Wizard.FINISH_RETURN_CODE == wizard_create.showModalDialog());
            assertTrue("Asset-create wizard has asset changes",
                    ((AssetEditor) wizard_create).getHasLocalChanges());
        } catch ( Throwable e) {
            olog_generic.log(Level.INFO, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            olib_asset.remove(a_new.getId());
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
                    SecurityAssetType.ACL);
            olib_asset.syncAsset(acl_everybody);
            final Map<String, UUID> v_homeids = m_search.getHomeAssetIds();
            final UUID u_home = v_homeids.get("littleware.test_home");
            final String s_foldername = "testGroupFolderTool";
            Asset a_folder = m_search.getAssetFromOrNull(u_home, s_foldername);

            if (null == a_folder) {
                final LittleGroup group_everybody =  m_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                        SecurityAssetType.GROUP);

                a_folder = AssetType.GENERIC.create();
                a_folder.setHomeId(u_home);
                a_folder.setAclId(acl_everybody.getId());
                a_folder.setFromId(u_home);
                a_folder.setName(s_foldername);
                a_folder = m_asset.saveAsset(a_folder, "Setting up test folder");

                LittleGroup group_copy = SecurityAssetType.GROUP.create();
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

