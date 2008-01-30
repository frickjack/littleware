package littleware.apps.test;

import com.google.inject.Inject;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.*;
import java.net.*;

import junit.framework.*;
import com.nexes.wizard.Wizard;

import littleware.asset.*;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.*;
import littleware.apps.swingclient.wizard.CreateAssetWizard;
import littleware.base.*;
import littleware.base.swing.*;
import littleware.security.auth.*;
import littleware.security.*;

/**
 * Tester for the SimpleXmlDataAsset Asset super class handling
 * of XML-based getData/setData via annotations.
 */
public class SwingClientTester extends TestCase {

    private static final Logger olog_generic = Logger.getLogger("littleware.apps.test.SwingClientTester");
    private final IconLibrary olib_icon;
    private final SessionHelper om_helper;
    private final AssetModelLibrary olib_asset;
    private final AssetViewFactory ofactory_view;
    private final AssetEditorFactory ofactory_edit;

    /**
     * Do nothing constructor - just pass the test-name through to super,
     * and stash managers needed for test.
     */
    public SwingClientTester(String s_test_name, SessionHelper m_helper,
            AssetModelLibrary lib_asset, IconLibrary lib_icon,
            AssetViewFactory factory_view,
            AssetEditorFactory factory_edit) {
        super(s_test_name);
        om_helper = m_helper;
        olib_asset = lib_asset;
        olib_icon = lib_icon;
        ofactory_view = factory_view;
        ofactory_edit = factory_edit;
    }
    
    /** Inject dependencies */
    @Inject
    public SwingClientTester( SessionHelper m_helper,
        AssetModelLibrary lib_asset, IconLibrary lib_icon,
        AssetViewFactory factory_view,
        AssetEditorFactory factory_edit) {
        this( "", m_helper, lib_asset, lib_icon, factory_view, factory_edit );
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

    /**
     * Run the injected AssetModelLibrary through a few simple tests
     */
    public void testAssetModelLibrary() {
        // couple bogus test assets - donot save to repository
        final Asset a_bogus1 = AssetType.GENERIC.create();
        final Asset a_bogus2 = AssetType.GENERIC.create();


        try {
            final Asset a_test = om_helper.getSession();
            final AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);

            olib_asset.remove(a_test.getObjectId());

            assertTrue("Simple sync is ok",
                    olib_asset.syncAsset(a_test).getAsset() == a_test);
            assertTrue("No retrieval if not necessary",
                    olib_asset.retrieveAssetModel(a_test.getObjectId(), m_search).getAsset() == a_test
                    );
            
            AssetModel amodel_everybody =
                    olib_asset.syncAsset( m_search.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP, 
                    SecurityAssetType.GROUP ) 
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
            olib_asset.remove( amodel_everybody.getAsset ().getObjectId () );
            assertTrue( "ModelLibrary getByName cleared after remove",
                    olib_asset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP
                    ) == null
                    );

            final AssetEditor edit_bogus = new AbstractAssetEditor(this) {

                public void eventFromModel(LittleEvent event_property) {
                    // just do something - anything
                    olog_generic.log( Level.INFO, "Test editor received event from model, setting value to 5" );
                    a_bogus2.setValue(5);
                }
            };
            edit_bogus.setAssetModel(olib_asset.syncAsset(a_bogus1)); //addPropertyChangeListener ( listen_assetprop );
            a_bogus2.setFromId(a_bogus1.getObjectId());
            // Adding a_bogus2 to the asset repository should trigger a Property.assetsLinkingFrom
            // property-change event on listeners to a_bogus1 AssetModel
            olib_asset.syncAsset(a_bogus2);
            Thread.sleep(4000); // let any asynchrony work itself out
            assertTrue("AssetModel cascading properties correctly", 5 == a_bogus2.getValue());

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            olib_asset.remove(a_bogus1.getObjectId());
            olib_asset.remove(a_bogus2.getObjectId());
        }
    }

    /**
     * Popup a JSessionManager for the tester to verify
     */
    public void testJSessionManager() {
        try {
            SessionManager m_session = SessionUtil.getSessionManager(SessionUtil.getRegistryHost(),
                    SessionUtil.getRegistryPort());

            final JSessionManager wm_session = new JSessionManager(m_session);
            wm_session.addLittleListener(
                    new LittleListener() {

                        public void receiveLittleEvent(LittleEvent event_little) {
                            String s_session_info = null;
                            if (event_little.isSuccessful()) {
                                try {
                                    UUID u_session = ((SessionHelper) event_little.getResult()).getSession().getObjectId();
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
     * Popup a JSessionHelper for the tester to verify 
     */
    public void testJSessionHelper() {
        try {
            JSessionHelper wm_helper = new JSessionHelper(om_helper);
            assertTrue("User confirmed JSesionHelper UI functional",
                    JLittleDialog.showTestDialog(wm_helper,
                    "play with the JSessionHelper widget. \n" +
                    "Hit OK when test successfully done"));

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
            LittleGroup group_everybody = (LittleGroup) m_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP);
            AssetModel model_everybody = olib_asset.syncAsset(group_everybody);
            olog_generic.log(Level.INFO, "GROUP SYNCED: " + AccountManager.LITTLEWARE_EVERYBODY_GROUP);

            JComponent wview_group = (JComponent) ofactory_view.createView(model_everybody);
            wview_group.setPreferredSize(new Dimension(800, 700));  // force big

            assertTrue("User confirmed group-viewer UI functional",
                    JLittleDialog.showTestDialog(wview_group,
                    "play with the GroupView widget. \n" +
                    "Hit OK when test successfully done"));

            LittleAcl acl_everybody = (LittleAcl) m_search.getByName(AclManager.ACL_EVERYBODY_READ,
                    SecurityAssetType.ACL);
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
            JComponent wbrowser_asset = new JAssetBrowser(ofactory_view,
                    olib_icon,
                    olib_asset,
                    m_search);
            SimpleAssetViewController listen_control = new ExtendedAssetViewController(
                    m_search,
                    om_helper.getService(ServiceType.ASSET_MANAGER),
                    olib_asset,
                    ofactory_edit, ofactory_view,
                    olib_icon,
                    om_helper.getSession());
            listen_control.setControlView((AssetView) wbrowser_asset);
            JSimpleAssetToolbar wtoolbar_asset = new JSimpleAssetToolbar(
                    olib_asset,
                    olib_icon,
                    m_search
                    );
            wtoolbar_asset.setConnectedView( (AssetView) wbrowser_asset );
            wtoolbar_asset.getButton(JSimpleAssetToolbar.ButtonId.CREATE).setEnabled(true);
            wtoolbar_asset.getButton(JSimpleAssetToolbar.ButtonId.EDIT).setEnabled(true);
            wtoolbar_asset.getButton(JSimpleAssetToolbar.ButtonId.DELETE).setEnabled(true);

            ((LittleTool) wtoolbar_asset).addLittleListener(listen_control);

            ((AssetView) wbrowser_asset).setAssetModel(model_asset);

            final JPanel wpanel_browser = new JPanel(new BorderLayout());
            wpanel_browser.add(wtoolbar_asset, BorderLayout.PAGE_START);
            wpanel_browser.add(wbrowser_asset, BorderLayout.CENTER);
            //wpanel_browser.setPreferredSize ( new Dimension ( 1000, 700 ) );  // force big

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
            AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);

            olib_asset.syncAsset(om_helper.getSession());

            if (true) { // Simple group editor
                LittleGroup group_everybody = (LittleGroup) m_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                        SecurityAssetType.GROUP);
                LittleGroup group_test = (LittleGroup) m_search.getByName("group.littleware.test_user",
                        SecurityAssetType.GROUP);
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
                        SecurityAssetType.ACL);
                if (null == acl_test) {
                    acl_test = (LittleAcl) m_search.getByName(AclManager.ACL_EVERYBODY_READ,
                            SecurityAssetType.ACL);
                    acl_test.setObjectId(UUIDFactory.getFactory().create());
                    acl_test.setName(s_acl_name);
                    Asset a_testhome = m_search.getByName("littleware.test_home", AssetType.HOME);
                    acl_test.setHomeId(a_testhome.getObjectId());
                    acl_test.setFromId(a_testhome.getObjectId());
                    acl_test.setComment("AclEditor test acl");
                    acl_test.setOwnerId(om_helper.getSession().getOwnerId());
                    om_helper.getService(ServiceType.ASSET_MANAGER).saveAsset(acl_test, "Setting up test asset");
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
        Asset a_new = AssetType.GENERIC.create();
        a_new.setName("test_asset");

        try {
            AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
            Wizard wizard_create = new CreateAssetWizard(
                    om_helper.getService(ServiceType.ASSET_MANAGER),
                    om_helper.getService(ServiceType.ASSET_SEARCH),
                    olib_asset,
                    olib_icon,
                    ofactory_view,
                    null,
                    olib_asset.syncAsset(a_new));
            assertTrue("User closed create-asset wizard ok",
                    Wizard.FINISH_RETURN_CODE == wizard_create.showModalDialog());
            assertTrue("Asset-create wizard has asset changes",
                    ((AssetEditor) wizard_create).getHasLocalChanges());
        } catch ( Throwable e) {
            olog_generic.log(Level.INFO, "Caught unexpected: " + e + ", " +
                    BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            olib_asset.remove(a_new.getObjectId());
        }
    }

    /**
     * Popup the JGroupsUnderParent tool
     */
    public void testGroupFolderTool() {
        try {
            final Factory<UUID> factory_uuid = UUIDFactory.getFactory();
            final AssetSearchManager m_search = om_helper.getService(ServiceType.ASSET_SEARCH);
            final AssetManager m_asset = om_helper.getService(ServiceType.ASSET_MANAGER);

            final LittleAcl acl_everybody = (LittleAcl) m_search.getByName(AclManager.ACL_EVERYBODY_READ,
                    SecurityAssetType.ACL);
            olib_asset.syncAsset(acl_everybody);
            final Map<String, UUID> v_homeids = m_search.getHomeAssetIds();
            final UUID u_home = v_homeids.get("littleware.test_home");
            final String s_foldername = "testGroupFolderTool";
            Asset a_folder = m_search.getAssetFromOrNull(u_home, s_foldername);

            if (null == a_folder) {
                final LittleGroup group_everybody = (LittleGroup) m_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                        SecurityAssetType.GROUP);

                a_folder = AssetType.GENERIC.create();
                a_folder.setHomeId(u_home);
                a_folder.setAclId(acl_everybody.getObjectId());
                a_folder.setFromId(u_home);
                a_folder.setName(s_foldername);
                a_folder = m_asset.saveAsset(a_folder, "Setting up test folder");

                LittleGroup group_copy = SecurityAssetType.GROUP.create();
                group_copy.sync(group_everybody);
                group_copy.setHomeId(u_home);
                group_copy.setName(s_foldername + ".copy1");
                group_copy.setObjectId(factory_uuid.create());
                group_copy.setFromId(a_folder.getObjectId());
                group_copy.setAclId(acl_everybody.getObjectId());
                group_copy.setOwnerId(a_folder.getOwnerId());
                m_asset.saveAsset(group_copy, "Setting up test GROUP 1");

                group_copy.setObjectId(factory_uuid.create());
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
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

