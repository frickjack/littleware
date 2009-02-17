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

import littleware.test.JLittleDialog;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.*;
import littleware.apps.client.*;
import littleware.apps.filebucket.*;
import littleware.apps.tracker.*;
import littleware.apps.tracker.swing.*;
import littleware.base.*;
import littleware.security.*;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.ServiceType;
import littleware.test.LittleTest;


/**
 * Tester for the task-tracking tools in
 * the littleware.apps.tracker package.
 */
public class TrackerTester extends LittleTest {

    private static final String os_test_data = "Frickjack bla bla " + (new Date()).getTime();
    private static final String os_test_folder = "TrackerTester";
    private static final Logger olog_generic = Logger.getLogger("littleware.apps.test.TrackerTester");
    private static Asset oa_test_folder = null;
    private AssetManager om_asset = null;
    private AssetSearchManager om_search = null;
    private BucketManager om_bucket = null;
    private AccountManager om_account = null;
    private final SessionHelper om_helper;
    private final Provider<JQView>  oprovide_view;
    private Set<Asset> ov_test = new HashSet<Asset>();

    /**
     * Constructor takes test name for superclass
     * and injects a couple dependencies
     */
    @Inject
    public TrackerTester(
            SessionHelper m_helper,
            Provider<JQView> provide_view
            ) {
        om_helper = m_helper;
        oprovide_view = provide_view;
    }
    

    /**
     * Setup the test folder to create test assets under.
     */
    @Override
    public void setUp() {
        try {
            om_asset = om_helper.getService(ServiceType.ASSET_MANAGER);
            om_search = om_helper.getService(ServiceType.ASSET_SEARCH);
            om_bucket = om_helper.getService(BucketServiceType.BUCKET_MANAGER);
            om_account = om_helper.getService(ServiceType.ACCOUNT_MANAGER);

            if (null == oa_test_folder) {
                UUID u_home = om_search.getHomeAssetIds().get("littleware.test_home");
                Map<String, UUID> v_children = om_search.getAssetIdsFrom(u_home, AssetType.GENERIC);
                UUID u_test_folder = v_children.get(os_test_folder);

                if (null == u_test_folder) {
                    Asset a_folder = AssetType.GENERIC.create();
                    a_folder.setFromId(u_home);
                    a_folder.setHomeId(u_home);
                    a_folder.setName(os_test_folder);
                    UUID u_acl = om_search.getByName(littleware.security.LittleAcl.ACL_EVERYBODY_READ, SecurityAssetType.ACL).getObjectId();

                    a_folder.setAclId(u_acl);
                    oa_test_folder = om_asset.saveAsset(a_folder, "setup folder for test");
                } else {
                    oa_test_folder = om_search.getAsset(u_test_folder);
                }
            }
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Failed setup, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw new AssertionFailedException("Failed setup, caught: " + e, e);
        }
    }

    /**
     * Delete the test assets
     */
    @Override
    public void tearDown() {
        for (Asset a_test : ov_test) {
            try {
                om_asset.deleteAsset(a_test.getObjectId(), "Cleanup after test");
            } catch (Exception e) {
                olog_generic.log(Level.WARNING, "Teardown caught: " + e);
            }
        }
        ov_test.clear();
    }

    /**
     * Try to write some data to a bucket under a test asset.
     */
    public littleware.apps.tracker.Queue buildQueueAndTest() {
        try {
            littleware.apps.tracker.Queue q_test = TrackerAssetType.QUEUE.create();
            Date t_now = new Date();

            {
                LittleUser p_user = om_account.getAuthenticatedUser();
                assertTrue("Authenticated: " + p_user.getName(), p_user.getName().equals("littleware.test_user"));
            }
            q_test.setHomeId(oa_test_folder.getHomeId());
            q_test.setName("test" + t_now.getTime());
            q_test.setAclId(oa_test_folder.getAclId());
            q_test.setFromId(oa_test_folder.getObjectId());
            q_test.save(om_asset, "setup new test");
            ov_test.add(q_test);

            Task task_1 = TrackerAssetType.TASK.create();
            long l_starting_transaction = task_1.getTransactionCount();
            task_1.setHomeId(oa_test_folder.getHomeId());
            task_1.setName("task_1_" + t_now.getTime());
            task_1.setAclId(oa_test_folder.getAclId());
            task_1.addToQueue(q_test);
            task_1.setComment("I am task_1");
            assertTrue("Task points to its queue", task_1.getToId().equals(q_test.getObjectId()));
            task_1.setTaskStatus(TaskStatus.WAITING_IN_Q);
            task_1.save(om_asset, "setup task1 in test queue");
            assertTrue("Transaction count adavances on save", task_1.getTransactionCount() > l_starting_transaction);
            ov_test.add(task_1);


            Map<UUID, Long> v_check = new HashMap();
            v_check.put(q_test.getObjectId(), q_test.getTransactionCount());
            v_check.put(task_1.getObjectId(), task_1.getTransactionCount());
            Map<UUID, Long> v_check_result = om_search.checkTransactionCount(v_check);

            assertTrue("Queue needs update after adding task", v_check_result.containsKey(q_test.getObjectId()));
            assertTrue("Task is up to date", !v_check_result.containsKey(task_1.getObjectId()));

            q_test.sync(om_search.getAsset(q_test.getObjectId()));

            Task task_2 = TrackerAssetType.TASK.create();
            task_2.setName("task_2_" + t_now.getTime());
            task_2.setAclId(task_1.getAclId());
            task_2.setHomeId(task_1.getHomeId());
            task_2.setComment("I am task_2");
            task_2.makeSubtaskOf(task_1);
            assertTrue("Subtask sets from id", task_2.getFromId().equals(task_1.getObjectId()));
            Dependency depend_1_2 = task_1.addDependency(task_2);
            task_2.save(om_asset, "Setup another test task");
            ov_test.add(task_2);
            depend_1_2.save(om_asset, "Setup task1 to task2 dependency");
            ov_test.add(depend_1_2);
            task_1.sync(om_search);

            List<UUID> v_subtask = task_1.getSubtask().get(TaskStatus.IDLE);
            assertTrue("IDLE subtask list not empty", !v_subtask.isEmpty());
            assertTrue("IDLE subtask list contains task_2", v_subtask.contains(task_2.getObjectId()));

            List<UUID> v_depend = task_1.getTaskIdDependingOn().get(TaskStatus.IDLE);
            assertTrue("IDLE depend list not empty", !v_depend.isEmpty());
            assertTrue("IDLE depend list contains task_2", v_depend.contains(task_2.getObjectId()));

            Comment comment_simple = task_1.addComment("Just a test");
            assertTrue("Comment points at its Task", comment_simple.getToId().equals(task_1.getObjectId()));
            comment_simple.saveComment(om_bucket, "Writing some goofy comment");
            ov_test.add(comment_simple);

            v_check.clear();
            v_check.put(task_1.getObjectId(), task_1.getTransactionCount());
            v_check_result = om_search.checkTransactionCount(v_check);
            assertTrue("Task needs sync after adding comment", v_check_result.containsKey(task_1.getObjectId()));
            task_1.sync(om_search);
            assertTrue("Task comment list has one entry", task_1.getTaskComments().size() == 1);
            assertTrue("Task contains expected comment: " + comment_simple.getObjectId() + " =? " + task_1.getTaskComments().get(0), task_1.getTaskComments().contains(comment_simple.getObjectId()));
            comment_simple.eraseComment(om_bucket);
            v_check.put(task_1.getObjectId(), task_1.getTransactionCount());
            v_check_result = om_search.checkTransactionCount(v_check);
            assertTrue("Task does not need sync after updating already added comment", !v_check_result.containsKey(task_1.getObjectId()));

            return q_test;
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e + ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
            throw new AssertionFailedException("Should never make it here");
        }
    }

    public void testTracker() {
        buildQueueAndTest();
    }

    /**
     * Test littleware.apps.tracker.swing components
     */
    public void testTrackerSwing() {
        try {
            littleware.apps.tracker.Queue q_test = buildQueueAndTest();
            AssetModel model_queue = (new SimpleAssetModelLibrary()).syncAsset(q_test);
            JQView wq_view = oprovide_view.get ();
            wq_view.setAssetModel( model_queue );
            assertTrue("User confirmed queue-viewer UI functional", JLittleDialog.showTestDialog(wq_view, "play with the queue view widget. \n" + "Hit OK when test successfully done"));
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e + ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}
