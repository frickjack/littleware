/// <reference path="../../libts/yui.d.ts" />


declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-eventTrack-littleToDo";
}

// little dance for hooking into YUI's module system
var Y: Y.YUI = exports;
// lw for accessing untyped littleware modules implements in javascript
var lw = exports.littleware;
import littleAsset = module("../asset/littleAsset" );
import asset = littleAsset.littleware.asset;


/**
 * @module littleware-eventTrack-littleApp
 * @namespace littleware.eventTrack.littleToDo
 */
export module littleware.eventTrack.littleToDo {
    
    var log = new lw.littleUtil.Logger("littleware.eventTrack.littleToDo");
    log.log("littleware logger loaded ...");

    var rootNode = asset.GenericAsset.GENERIC_TYPE.newBuilder().withName("littleToDo"
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
    function getDataFolder(): asset.Asset {
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
        static TODO_TYPE = new asset.AssetType("A2985AD5556242D28B9FBA65789F21F6", -1, "littleware.TODO");

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
        var builder = new asset.AssetBuilder(ToDoItem.TODO_TYPE);
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

    asset.AssetType.register(ToDoItem.TODO_TYPE);

    //-------------------------------------

    /**
     * Application-level interface to TODO data.
     * Wraps read-only backend data model, and allows listeners to
     * register to be informed when the data changes.
     * Client can change a todo's properties via the ToDoManager.
     */
    export class ToDoSummary {
        constructor(public item: ToDoItem) {}

        /**
         * @method getId
         */
        getId(): string {
            return this.item.getId();
        }

        /**
         * @method getLabel
         */
        getLabel(): string { return this.item.getName(); }

        getDescription(): string { return this.item.getComment(); }

        addListener(F: (any) => any): void {
            Y.assert(false, "not yet implemented");
        }
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
         * @return {Asset}
         */
        getUserDataFolder(): asset.Asset;

        /**
         * Get the active child-tasks (not grandchildren, etc) under the given parent
         * @method getActiveToDos
         * @param parentId {string}
         * @reutrn {Promise{Array{ToDoItem}}}
         */
        getActiveToDos(parentId: string): ToDoItem[];

        /**
         * @method updateToDo
         */
        updateToDo( id: string, name: string, description: string, state: number, comment: String): ToDoItem;

        /**
         * @method newToDo
         */
        newToDo( parentId:string, name: string, description: string): ToDoItem;
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
                            asset.IdFactories.get().get()
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
