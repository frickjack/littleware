/// <reference path="../../libts/yui.d.ts" />


declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-eventTrack-toDoAPI";
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
 * @module littleware-eventTrack-toDoAPI
 * @namespace littleware.eventTrack.toDoAPI
 */          
export module littleware.eventTrack.toDoAPI {
    
    var log = new lw.littleUtil.Logger("littleware.eventTrack.toDoAPI");
    log.log("littleware logger loaded ...");


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
         * Alias for getToId
         * returns the "ToDoParent" id - which is not the "fromId", because
         * ToDo's are organized under /active and /archived subtrees,
         * so a structure like /parent/active/child1 or /parent/archive/year/month/child2
         * is typical.  
         * @method getParentToDoId
         * @return {string} id of parent if any - parent may not necessarily be a ToDoItem itself ...
         */
        getParentToDoId(): string { return this.getToId(); }

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
            public children: axMgr.NameIdListRef,
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

        getParentId(): string { return this.ref.getAsset().getToId(); }

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
         * @return {Y.Promise{AssetRef}}
         */
        getUserDataFolder(): Y.Promise;

        /**
         * Get the active child-tasks (not grandchildren, etc) under the given parent
         * @method getActiveToDos
         * @param parentId {string}
         * @reutrn {Promise{Array{ToDoSummary}}}
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
         * @param id {string}
         * @param props {[key:string]}
         * @return {Y.Promise{ToDoSummary}}
         */
        updateToDo(id: string,
            props: { name?: string; description?: string; state?: number; }
            ): Y.Promise;

        /**
         * Factory for ToDoSummary
         * @method createToDo
         * @return {Y.Promise{ToDoSummary}} new ToDo saved to the repository
         */
        createToDo(parentId: string, name: string, description: string): Y.Promise;
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
                        if (ref.isDefined()) {
                            return this.axTool.listChildren(ref.getAsset().getId());
                        } else {
                            return Y.when( new axMgr.NameIdListRef([]) );
                        }
                    }
                ).then(
                    (children: axMgr.NameIdListRef) => {
                        //log.log("loadActiveToDos active children list size: " + children.size() + ", " + JSON.stringify( children.copy() ) );
                        //Y.assert( children.copy().length === children.size(), "List copy behaves as expected");
                        var batch = Y.Array.map(children.copy(), (it: axMgr.NameIdPair) => { return this.axTool.loadAsset(it.getId()); });
                        Y.assert(batch.length == children.size(), "batch setup looks ok");
                        return Y.batch.apply(Y, batch);
                    }
                ).then(
                    (refs: ax.AssetRef[]) => {
                        //log.log("loadActiveToDos child-load batch size: " + refs.length);
                        var assets: ToDoItem[] = [];
                        for (var i = 0; i < refs.length; ++i) {
                            if (refs[i].isDefined()) {
                                assets.push(refs[i].getAsset() );
                            }
                        }
                        log.log("Loading active todos: " + assets.length);
                        return Y.batch.apply(Y,
                            Y.Array.map(assets,
                                (todo: ToDoItem) => {
                                    return this.loadToDo(todo.getId());
                                }
                            )
                          );
                    }
                );
        }


        private _todoCache: { [key: string]: ToDoSummary; } = {};

        loadToDo(id: string): Y.Promise {
            //log.log("Loading ToDo summary: " + id);
            if (this._todoCache[id]) {
                return Y.when( this._todoCache[id] );
            }
            return this.axTool.loadAsset(id).then(
                (ref: axMgr.AssetRef) => {
                    if (ref.isDefined()) {
                        var todo:ToDoItem = ref.getAsset();
                        return this.axTool.loadSubpath(todo.getId(), activeFolderName
                            ).then(
                                (ref: axMgr.AssetRef) => {
                                    if (ref.isDefined()) {
                                        return this.axTool.listChildren(ref.getAsset().getId());
                                    } else {
                                        return Y.when( new axMgr.NameIdListRef( [] ) );
                                    }
                                }
                            ).then(
                                (children: axMgr.NameIdListRef) => {
                                    var result = new ToDoSummary(ref, children, this);
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

        private _isArchiveState(state: number): bool {
            return ((state === ToDoItem.STATE.CANCELED) || (state === ToDoItem.STATE.COMPLETE));
        }

        private _monthString(date: Date): string {
            var month = "00" + date.getMonth();
            month = month.substring(month.length - 2);
            return "" + date.getFullYear() + month;
        }

        updateToDo(id: string,
            props: { name?: string; description?: string; state?: number; }
            ): Y.Promise {
            // TODO - clean this up to handle different scenarios - todo goes inactive, but has children, ...
            return this.axTool.loadAsset(id).then(
                (ref: axMgr.AssetRef) => {
                    Y.assert(ref.isDefined(), "ToDo exists with id: " + id);
                    Y.assert(ref.getAsset().getAssetType().id == ToDoItem.TODO_TYPE.id, "asset must have ToDoItem type: " + id);
                    var builder: ax.AssetBuilder = ref.getAsset().copy();
                    if ( props.name ) { builder.name = props.name; }
                    if ( props.description) { builder.comment = props.description; }
                    if ((props.state === 0) || props.state) { builder.state = props.state; }

                    Y.assert( builder.toId && true, "todo has pointer to parent");

                    //
                    // posibly move ToDo to a different folder (active or archive) -
                    // initial default is no move
                    //
                    var moveFolderStep: Y.Promise = Y.when(builder.fromId);

                    if ( this._isArchiveState( builder.state) && (! this._isArchiveState(ref.getAsset().getState()))
                         ) {
                        // move asset under parent's archive folder
                        var now = new Date();
                        
                        var archiveBranch = [
                            {
                                name: "archive",
                                builder: (parent) => { return ax.GenericAsset.GENERIC_TYPE.newBuilder(); }
                            },
                            {
                                name: this._monthString( now ),
                                builder: (parent) => { return ax.GenericAsset.GENERIC_TYPE.newBuilder(); }
                            }
                        ];
                        moveFolderStep = this.axTool.buildBranch(builder.toId, archiveBranch).then(
                            (refs: ax.AssetRef[]) => {
                                return refs[refs.length - 1].getAsset().getId();
                            }
                        );
                        // give asset a unique name for archive
                        builder.name = builder.name + "-" + now.getTime();
                    } else if ( (!this._isArchiveState( builder.state)) && this._isArchiveState(ref.getAsset().getState()) ) {
                        // move asset back under parent's active folder
                        var activeBranch = [
                            {
                                name: activeFolderName,
                                builder: (parent) => {
                                    return ax.GenericAsset.GENERIC_TYPE.newBuilder();
                                }
                            }
                        ];
                        moveFolderStep = this.axTool.buildBranch(builder.toId, activeBranch).then(
                            (refs: ax.AssetRef[]) => {
                                return refs[refs.length - 1].getAsset().getId();
                            }
                        );
                    }
                    return moveFolderStep.then(
                            (fromId: string) => { return this.axTool.saveAsset(builder.withFromId(fromId).build(), builder.comment); }
                        ).then(
                            (ref: axMgr.AssetRef) => {
                                return this.loadToDo(ref.getAsset().getId());
                            }
                        );
                }
             );
        }

        createToDo(parentId: string, name: string, description: string): Y.Promise {
            var branch = [
                {
                    name: activeFolderName,
                    builder: (parent) => {
                        return ax.GenericAsset.GENERIC_TYPE.newBuilder();
                    }
                },
                {
                    name: name,
                    builder: (parent) => {
                        return ToDoItem.TODO_TYPE.newBuilder().withComment(description).withToId(parentId);
                    }
                },
                {
                    name: activeFolderName,
                    builder: (parent) => {
                        return ax.GenericAsset.GENERIC_TYPE.newBuilder();
                    }
                }

            ];

            // First - load parent, make sure it's a TODO ...
            return this.axTool.loadAsset(parentId).then(
                (ref: axMgr.AssetRef) => {
                    Y.assert(ref.isDefined(), "unable to load todo parent: " + parentId);
                    return this.axTool.buildBranch(parentId, branch
                        ).then(
                            (refs: ax.AssetRef[]) => {
                                Y.assert(refs.length == 3, "createToDo got expected branch result");
                                var todoRef = refs[1];
                                Y.assert(todoRef.getAsset().getAssetType().id === ToDoItem.TODO_TYPE.id,
                                    "createToDo branch-create setup is as expected"
                                    );
                                return new ToDoSummary(todoRef, new axMgr.NameIdListRef([]), this);
                            }
                        );
                }
              );
        }

    }

    // setup ToDo data folder
    var singleton: SimpleManager = null;

    /**
     * Get the ToDo manager singleton ...
     * @method getToDoManager
     * @for
     */
    export function getToDoManager(): ToDoManager {
        if (singleton) {
            return singleton;
        }
        var tool: axMgr.AssetManager = axMgr.getAssetManager();
        var branch = [
            {
                name: "littleToDo",
                builder: (parent) => {
                    return ax.HomeAsset.HOME_TYPE.newBuilder().withComment("root for littleToDo data");
                }
            },
            {
                name: "user",
                builder: (parent) => {
                    return ax.GenericAsset.GENERIC_TYPE.newBuilder().withComment("place holder for eventual user name");
                }
            }
        ];
        var folderPromise: Y.Promise = tool.buildBranch(null, branch).then((refs: ax.AssetRef[]) => {
            return refs[refs.length - 1];
        }
        );
        singleton = new SimpleManager(tool, folderPromise);
        return singleton;
    }

    //-----------------------------------

    export function buildTestSuite(): Y.Test_TestSuite {
        var suite: Y.Test_TestSuite = new Y.Test.TestSuite("littleware-toDoAPI");
        suite.add(new Y.Test.TestCase(
                {
                    testToDoBuild: function () {
                        var parent =  ax.GenericAsset.GENERIC_TYPE.newBuilder().withName("littleToDo"
                            ).withComment("Root node for littleToDo data tree"
                            ).withFromId( "bogus"  // just for testing
                            ).build();

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
                    },

                    // create a couple todos, archive, etc.
                    testToDoManager: function () {
                        var tool: ToDoManager = getToDoManager();

                        var createTestToDo = (parentId: string, logLabel:string ) => {
                            return tool.createToDo(parentId, "TestToDo", "test ToDo"
                               ).then(
                                    (todo: ToDoSummary) => {
                                        return tool.loadActiveToDos(todo.getParentId());
                                    }
                                ).then(
                                    (todoList: ToDoSummary[]) => {
                                        log.log( "active todos list size: " + todoList.length );
                                        for (var i = 0; i < todoList.length; ++i) {
                                            log.log( "active todo: " + todoList[i].getLabel() );
                                        }
                                        var todo = Y.Array.find(todoList, (it: ToDoSummary) => {
                                            return (it.getLabel() === "TestToDo");
                                        });
                                        Y.Assert.isTrue(todo && true, logLabel + " found TestToDo in active list");
                                        return todo;
                                    }
                                );
                        };

                        tool.getUserDataFolder().then(
                            // create test to-do
                            (ref: axMgr.AssetRef) => {
                                return createTestToDo(ref.getAsset().getId(), "root test:" );
                            }
                          ).then(
                            // create a child to-do under the first to-do
                            (todo: ToDoSummary) => {
                                return createTestToDo(todo.getId(), "2nd level:" );
                            }
                          ).then(
                            // finally, cleanup - archive the 1st todo
                            (child: ToDoSummary) => {
                                return tool.updateToDo(child.getParentId(), {
                                    state: ToDoItem.STATE.COMPLETE, comment: "archive complete TODO"
                                }
                                    );
                            }
                          ).then(
                            (todo: ToDoSummary) => {
                                log.log("checking that archived todo is out of active list ...");
                                return tool.loadActiveToDos(todo.getParentId());
                            }
                          ).then(
                            (todoList: ToDoSummary[]) => {
                                this.resume(() => {
                                    var shouldNotFind = Y.Array.find(todoList, (it: ToDoSummary) => {
                                        return (it.getLabel() == "TestToDo");
                                    });
                                    Y.Assert.isTrue(!(shouldNotFind && true), "Archived ToDo should not show up in active list");
                                });
                            },
                            (err) => {
                                this.resume(() => {
                                    throw err;
                                });
                            }
                          );
                        this.wait(5000);
                    }
                }
            ));
        return suite;
    }


    log.log("module initialized ...");
}
