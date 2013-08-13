/// <reference path="../../libts/yui.d.ts" />


declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-eventTrack-littleToDo";
}

// little dance for hooking into YUI's module system
var Y: Y = exports;

// lw for accessing untyped littleware modules implements in javascript
var lw = exports.littleware;
import littleAsset = module("../asset/littleAsset");
import assetMgr = module("../asset/assetMgr");

import ax = littleAsset.littleware.asset;
import axMgr = assetMgr.littleware.asset.manager;

/**
 * @module littleware-eventTrack-littleApp
 * @namespace littleware.eventTrack.littleToDo
 */
export module littleware.eventTrack.littleToDo {
    
    var log = new lw.littleUtil.Logger("littleware.eventTrack.littleToDo");
    log.log("littleware logger loaded ...");

    var rootNode = ax.GenericAsset.GENERIC_TYPE.newBuilder().withName("littleToDo"
        ).withComment("Root node for littleToDo data tree"
        ).build();



    /**
     * Just a place holder for now.  Each user has a data-folder
     * for her TODO data in our imagined eventual global TODO database.  
     * Bla.
     * @method dataFolderId
     * @return {string} folder id
     * @static 
     */
    function getDataFolder(): ax.Asset {
        return rootNode;
    }
    

    /**
     * Data node in the user's tree of TODOs extends littleware's Asset class.
     * A ToDo node for now just leverages the asset's id, name, comment, and state
     * properties.  The TaskManager maintains the tree of tasks in a consistent state -
     * all updates to the task node tree occur through the task manager.
     *
     * @class ToDoItem
     */
    export class ToDoItem extends littleAsset.littleware.asset.Asset {
        static TODO_TYPE = new ax.AssetType("A2985AD5556242D28B9FBA65789F21F6", -1, "littleware.TODO");

        /**
         * STATE enumerates the different states currently supported:
         *    WAITING, UNASSIGNED, ASSIGNED, RUNNING, CANCELED, COMPLETE,
         * so use in builder like this: 
         *     item.copy().withState( ToDoItem.STATE.RUNNING )
         * A node with subtasks derives its state from the subtask states -
         * WAITING on subtask states, then COMPLETE when all subtasks COMPLETE or CANCELED.
         *
         * @property STATE
         */
        static STATE = {
            WAITING:0,
            UNASSIGNED: 1,
            ASSIGNED: 2,
            RUNNING: 3,
            CANCELED: 4,
            COMPLETE: 5
        };

    }

    
    ToDoItem.TODO_TYPE.newBuilder = function () {
        var builder = new ax.AssetBuilder(ToDoItem.TODO_TYPE);
        builder.build = function () {
            Y.assert(builder.fromId || false, "TODO must have non-null fromId");

            var STATE = ToDoItem.STATE;
            if ((builder.state == STATE.RUNNING) && (!builder.startDate)) {
                builder.startDate = new Date();
            } else if (
                ((builder.state == STATE.CANCELED) || (builder.state == STATE.COMPLETE)) && (!builder.endDate)
                ) {
                builder.endDate = new Date();
            } else {
                builder.endDate = null;
                builder.startDate = null;
            }
            return new ToDoItem(builder);
        };
        return builder;
    }

    ax.AssetType.register(ToDoItem.TODO_TYPE);

    //-------------------------------------

    /**
     * Application-level interface to TODO data.
     * Wraps read-only backend data model, and allows listeners to
     * register to be informed when the data changes.
     * Client can change a todo's properties via the ToDoManager.
     */
    export class ToDoSummary {
        constructor(
            public ref: axMgr.AssetRef,
            public children: axMgr.RONameIdList,
            mgr:ToDoManager
            ) { }

        /**
         * @method getId
         */
        getId(): string {
            return this.ref.getAsset().getId();
        }

        /**
         * @method getLabel
         */
        getLabel(): string { return this.ref.getAsset().getName(); }

        getDescription(): string { return this.ref.getAsset().getComment(); }

    }

    //-------------------------------------

    /**
     * Tool for interacting with TODO subtree in the asset repository.
     * Each TODO organizes its subtasks into sub-folders based on state:
     *    todo-name/active  <br />
     *    todo-name/complete <br />
     *    todo-name/comments <br />
     * @class ToDoManager
     */
    export interface ToDoManager {
        /**
         * Get the root under which the manager builds the ToDo subtree for
         * the currently authenticated user
         * @method getUserDataFolder
         * @return {Y.Promise{Asset}}
         */
        getUserDataFolder(): Y.Promise;

        /**
         * Get the active child-tasks (not grandchildren, etc) under the given parent
         * @method getActiveToDos
         * @param parentId {string}
         * @reutrn {Promise{Array{ToDoItem}}}
         */
        loadActiveToDos(parentId: string): Y.Promise;

        /**
         * Load the ToDo with the given id
         * @param id {string}
         * @return {Y.Promise{ToDoSummary}}
         */
        loadToDo(id: string): Y.Promise;

        /**
         * Helper for updating ToDo model data in response to typical UI events
         * @method updateToDo
         * @return {Y.Promise{ToDoSummary}}
         */
        updateToDo( id: string, name: string, description: string, state: number, comment: String): Y.Promise;

        /**
         * Factory for ToDoSummary
         * @method newToDo
         * @return {Y.Promise{ToDoSummary}} new ToDo saved to the repository
         */
        newToDo(parentId: string, name: string, description: string): Y.Promise;
    }

    //-----------------------------------

    var activeFolderName: string = "active";

    class SimpleManager implements ToDoManager {
        constructor(
            private axTool: axMgr.AssetManager,
            private folderPromise: Y.Promise
            ) { }

        getUserDataFolder(): Y.Promise {
            return this.folderPromise;
        }

        loadActiveToDos(parentId: string): Y.Promise {
            return this.axTool.loadChild(parentId, activeFolderName
                ).then(
                    (ref: axMgr.AssetRef) => {
                        return this.axTool.listChildren(ref.getAsset().getId());
                    }
                ).then(
                    (children: axMgr.RONameIdList) => {
                        return Y.batch.apply(Y,
                            Y.Array.each(children.copy(), (it: axMgr.NameIdPair) => { return this.axTool.loadAsset(it.getId()); })
                        );
                    }
                ).then(
                    (refs: ax.AssetRef[]) => {
                        var assets: ToDoItem[] = [];
                        for (var i = 0; i < assets.length; ++i) {
                            if (refs[i].isDefined()) {
                                assets.push(refs[i].getAsset() );
                            }
                        }
                        return Y.batch.apply( Y,
                            Y.Array.each(assets,
                                (todo: ToDoItem) => {
                                    return this.loadToDo( todo.getId() );
                                }
                            )
                          );
                    }
                );
        }


        private _todoCache: { [key: string]: ToDoSummary; } = {};

        loadToDo(id: string): Y.Promise {
            // TODO: add cache!!!
            if ( this._todoCache[id]) {
                return Y.when( this._todoCache[id] );
            }
            return this.axTool.loadAsset(id).then(
                (ref: axMgr.AssetRef) => {
                    if (ref.isDefined()) {
                        var todo = ref.getAsset();
                        this.axTool.loadSubpath(todo.getId(), activeFolderName
                            ).then(
                                (ref: axMgr.AssetRef) => {
                                    if (ref.isDefined()) {
                                        return this.axTool.listChildren(ref.getAsset().getId());
                                    } else {
                                        return Y.when( new axMgr.RONameIdList( [] ) );
                                    }
                                }
                            ).then(
                                (children: axMgr.RONameIdList) => {
                                    var result = new ToDoSummary(todo, children, this);
                                    this._todoCache[todo.getId()] = result;
                                    return result;
                                }
                            );
                    } else {
                        return new Y.Promise((resolve, reject) => {
                            reject("no todo with id: " + id);
                        });
                    }
                }
              );
        }

        updateToDo(id: string, name: string, description: string, state: number, comment: String): Y.Promise {
            return this.axTool.loadAsset(id).then(
                (ref: axMgr.AssetRef) => {
                    if (ref.isDefined()) {
                    }
                }
             );
        }

        newToDo(parentId: string, name: string, description: string): Y.Promise { 
            return this.axTool.buildBranch(parentId, [
                {
                    name: name,
                    builder: (parent) => {
                        return ToDoItem.TODO_TYPE.newBuilder().withComment(description);
                    }
                },
                {
                    name: activeFolderName,
                    builder: (parent) => {
                        return ax.GenericAsset.GENERIC_TYPE.newBuilder();
                    }
                }
            ]
                ).then(
                    (refs: ax.AssetRef[]) => {
                        var itemRef = refs[0];
                        return new ToDoSummary(itemRef, new axMgr.RONameIdList([]), this);
                    }
                );
        }

    }

    

    //-----------------------------------

    export function buildTestSuite(): Y.Test_TestSuite {
        var suite: Y.Test_TestSuite = new Y.Test.TestSuite("littleware-asset");
        suite.add(new Y.Test.TestCase(
                {
                    testToDoBuild: function () {
                        var parent = getDataFolder();
                        var list_1 = ToDoItem.TODO_TYPE.newBuilder().withName("list_1"
                            ).withComment("todo test list 1").withParent(parent).build();
                        var todo_1 = ToDoItem.TODO_TYPE.newBuilder().withName("list_1_todo_1"
                            ).withComment("todo test list 1 item 1").withParent(list_1).build();
                        var todo_2 = todo_1.copy().withId(
                            ax.IdFactories.get().get()
                            ).withName("list_1_todo_2").withComment("todo test list 1 item 2").build();

                        Y.Assert.isTrue(todo_2.getFromId() == todo_1.getFromId(),
                                     "todos have same list as parent"
                                    );
                    }
                }
            ));
        return suite;
    }

}
