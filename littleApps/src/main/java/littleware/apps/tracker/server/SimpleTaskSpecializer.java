/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.server;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.apps.tracker.Queue;
import littleware.apps.tracker.Task;
import littleware.apps.tracker.TaskQuery;
import littleware.apps.tracker.TaskQuery.BuilderStart;
import littleware.apps.tracker.TaskQueryManager;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetTreeTemplate.AssetInfo;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.server.NullAssetSpecializer;
import littleware.base.AbstractValidator;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.ValidationException;
import org.joda.time.DateTime;
import org.joda.time.ReadableDateTime;

/**
 * TASK-type specializer manages server-side association of task with queue.
 * The BundleActivator mixin registers the specializer singleton with the
 * specializer registry.
 */
@Singleton
public class SimpleTaskSpecializer extends NullAssetSpecializer {

    private static final Logger log = Logger.getLogger(SimpleTaskSpecializer.class.getName());
    private final AssetSearchManager search;
    private final TaskQueryManager queryManager;
    private final Provider<BuilderStart> queryBuilder;
    private final AssetManager assetMgr;
    private final Provider<TemplateBuilder> treeBuilder;

    @Inject
    public SimpleTaskSpecializer(AssetSearchManager search,
            AssetManager assetMgr,
            TaskQueryManager queryManager,
            Provider<TaskQuery.BuilderStart> queryBuilder,
            Provider<AssetTreeTemplate.TemplateBuilder> treeBuilder) {
        this.search = search;
        this.assetMgr = assetMgr;
        this.queryManager = queryManager;
        this.queryBuilder = queryBuilder;
        this.treeBuilder = treeBuilder;
    }

    /**
     * Assign a queue-based name to the asset, and place it in the
     * queue node hierarchy
     */
    @Override
    public void postCreateCallback(Asset asset) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Task task = asset.narrow();
        final Queue queue = search.getAsset(task.getQueueId()).get().narrow();
        final Task.TaskBuilder builder = task.copy();

        if (!task.getQueueId().equals(task.getFromId())) {
            // TODO - add subtask logic
            final Maybe<Asset> maybeFrom = search.getAsset(task.getFromId());
            if (maybeFrom.isEmpty() || !(maybeFrom.get() instanceof Task)) {
                throw new ValidationException("New task does not link from queue or task");
            }
            final Task from = maybeFrom.get().narrow();
            if (!from.getQueueId().equals(task.getFromId())) {
                throw new ValidationException("New subtask queue-id does not match parent task queue-id");
            }
            builder.fromId(from.getId());
        } else {
            final ReadableDateTime now = new DateTime();
            final AssetTreeTemplate template = treeBuilder.get().assetBuilder("Archive").addChildren(
                        treeBuilder.get().assetBuilder(Integer.toString(now.getYear())).addChildren(
                            treeBuilder.get().assetBuilder(now.toString("MMdd")).build()
                            ).build()
                          ).build();

            UUID lastId = null;
            for (AssetInfo info : template.visit(queue, search)) {
                lastId = info.getAsset().getId();
                if (!info.getAssetExists()) {
                    assetMgr.saveAsset(info.getAsset(), "Setup queue tree");
                }
            }
            builder.fromId(lastId);
        }
        int taskNumber = -1;
        for (int i = 0; i < 100; ++i) {
            final TaskQuery query = queryBuilder.get().queue(queue).withTaskName(Integer.toString(queue.getNextTaskNumber() + i)).build();
            if (queryManager.runQuery(query).isEmpty()) {
                taskNumber = queue.getNextTaskNumber() + i;
                break;
            }
        }
        if (taskNumber < 0) {
            throw new IllegalStateException("Failed to find unused task-number under queue " + queue.getName() + " (" + queue.getId() + ")");
        }
        builder.name(Integer.toString(taskNumber));
        assetMgr.saveAsset(queue.copy().value(taskNumber + 1).build(),
                "Advance queue task number");

        assetMgr.saveAsset(
                builder.build(),
                "Reposition task in queue asset tree with unique name");
    }

    /**
     * If the name changes - make sure it's unique
     */
    @Override
    public void postUpdateCallback(Asset old, Asset current) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Task oldTask = old.narrow();
        final Task currentTask = current.narrow();
        AbstractValidator.assume(oldTask.getQueueId().equals(currentTask.getQueueId()),
                "May not move a task between queues");

        final Queue queue = search.getAsset(currentTask.getQueueId()).get().narrow();
        if (!old.getName().equals(current.getName())) {
            final TaskQuery query = queryBuilder.get().queue(queue).withTaskName(currentTask.getName()).build();
            AbstractValidator.assume(
                    queryManager.runQuery(query).size() <= 1,
                    "May not have more than one task with the same name " + currentTask.getName()
                    + " under queue " + queue.getName() + " (" + queue.getId() + ")");
        }
    }
}
