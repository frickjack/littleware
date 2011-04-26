/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.tracker.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Queue.QueueBuilder;
import littleware.apps.tracker.Task;
import littleware.apps.tracker.Task.TaskBuilder;
import littleware.apps.tracker.TaskQuery;
import littleware.apps.tracker.TaskQuery.BuilderStart;
import littleware.apps.tracker.TaskQueryManager;
import littleware.apps.tracker.TaskStatus;
import littleware.asset.Asset;
import littleware.asset.client.AssetManager;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.test.AbstractAssetTest;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.security.LittleUser;

/**
 * Test TaskQueryManager
 */
public class QueryManagerTester extends AbstractAssetTest {
    private static final Logger log = Logger.getLogger( QueryManagerTester.class.getName() );
    private static final String queueName = "QueueTest";
    private final TaskQueryManager queryManager;
    private final Provider<BuilderStart> provideQuery;
    private final AssetPath     queuePath;
    private final AssetPathFactory pathFactory;
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final LittleUser user;
    private final Provider<QueueBuilder> queueProvider;
    private final Provider<TaskBuilder> taskProvider;

    @Inject
    public QueryManagerTester( TaskQueryManager queryManager,
            Provider<TaskQuery.BuilderStart> provideQuery,
            AssetPathFactory pathFactory,
            AssetSearchManager search,
            AssetManager  assetMgr,
            LittleUser user,
            Provider<Queue.QueueBuilder> queueProvider,
            Provider<Task.TaskBuilder> taskProvider
            ) {
        this.queryManager = queryManager;
        this.provideQuery = provideQuery;
        this.pathFactory = pathFactory;
        this.search = search;
        this.assetMgr = assetMgr;
        this.user = user;
        this.queueProvider = queueProvider;
        this.taskProvider = taskProvider;
        try {
            this.queuePath = pathFactory.createPath("/" + getTestHome() + "/" + queueName );
        } catch ( Exception ex ) {
            throw new AssertionFailedException( "Failed to initialize test queue path", ex );
        }
        setName( "testQueryManager" );
    }

    @Override
    public void setUp() {
        try {
            final Option<Asset> maybe = search.getAssetAtPath(queuePath);
            if ( maybe.isSet() ) {
                return;
            }
            final Queue.QueueBuilder qbuilder = queueProvider.get();
            assetMgr.saveAsset(
                    qbuilder.parent( getTestHome( search ) ).name( queueName ).build(),
                    "Test setup"
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Setup failed", ex );
            fail( "Caught " + ex );
        }
    }

    public void testQueryManager() {
        try {
            final Date  startTime = new Date();
            final Queue queue = search.getAssetAtPath(queuePath).get().narrow();
            // Setup a few test tasks
            final Task task1 = assetMgr.saveAsset(
                    taskProvider.get().queue( queue ).build(),
                    "Setup test"
                    );
            assertTrue( "Task status defaults to complete: " + task1.getTaskStatus(),
                    task1.getTaskStatus().equals( TaskStatus.COMPLETE )
                    );
            final Task task2 = assetMgr.saveAsset(
                    taskProvider.get().queue( queue ).taskStatus( TaskStatus.UNASSIGNED ).build(),
                    "Setup test"
                    );
            assertTrue( "Task id increments: " + task1.getName() + ", " + task2.getName(),
                    Integer.parseInt( task1.getName() ) < Integer.parseInt( task2.getName() )
                    );
            assertTrue( "Task in unassigned state: " + task2.getTaskStatus(),
                    task2.getTaskStatus().equals( TaskStatus.UNASSIGNED ) );
            final Task task3 = assetMgr.saveAsset(
                    taskProvider.get().queue( queue ).taskStatus( TaskStatus.ASSIGNED ).assignTo(user).build(),
                    "Setup test"
                    );
            assertTrue( "Task assigned", task3.getTaskStatus().equals( TaskStatus.ASSIGNED ) );

            {
                final TaskQuery query = provideQuery.get().queue(queue).withTaskName( task1.getName() ).build();
                final Collection<UUID> result = queryManager.runQuery(query);
                assertTrue( "Got single result to query for " + task1.getName() + ": " + result.size(),
                        1 == result.size()
                        );
                assertTrue( "Unexpected result id: " + result.iterator().next(),
                        result.iterator().next().equals( task1.getId() )
                        );
            }
            log.log( Level.INFO, "Sleeping few seconds before testing duration query ..." );
            Thread.sleep( 3000 );
            final Date now = new Date();
            {
                final TaskQuery query = provideQuery.get().queue(queue).anyStatus().minCreateDate(startTime).maxCreateDate(now).build();
                final Collection<UUID> result = queryManager.runQuery(query);
                assertTrue( "Create-range query returns 3 test tasks",
                        3 == result.size()
                        );
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed", ex );
            fail( "Caught " + ex );
        }
    }
}
