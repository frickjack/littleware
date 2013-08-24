/// <reference path="../../libts/yui.d.ts" />


declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-eventTrack-toDoApp";
}

// little dance for hooking into YUI's module system
var Y: Y = exports;


// lw for accessing untyped littleware modules implements in javascript
var lw = exports.littleware;
import importLittleAsset = module("../asset/littleAsset");
import importAssetMgr = module("../asset/assetMgr");
import importToDo = module( "toDoAPI" );
import importToDoView = module("toDoView");
import importLittleApp = module( "littleApp" );

import ax = importLittleAsset.littleware.asset;
import axMgr = importAssetMgr.littleware.asset.manager;
import toDo = importToDo.littleware.eventTrack.toDoAPI;
import toDoView = importToDoView.littleware.eventTrack.toDoView;
import littleApp = importLittleApp.littleware.eventTrack.littleApp;


/**
 * @module littleware-eventTrack-toDoApp
 * @namespace littleware.eventTrack.toDoApp
 */          
export module littleware.eventTrack.toDoApp {
    var log = new lw.littleUtil.Logger("littleware.eventTrack.toDoApp");
    log.log("littleware logger loaded ...");

    /**
     * Internal helper manages data juggling on route changes
     * @class AppController
     */
    export class AppController {
        constructor(
            private todoTool: toDo.ToDoManager,
            private app: littleApp.ViewManager.Manager,
            private assetTool: axMgr.AssetManager
            ) { }

        /**
         * Listener passed to app.registerPanel at app setup time - 
         *  updates panel path and ToDo data model of new path on view change.
         * @method viewListener
         * @param panelStatus {PanelStatus}
         */
        viewListener(panelStatus: littleApp.PanelStatus):void {
            if ( panelStatus.state.name === "VISIBLE" ) {
                var oldPath = panelStatus.panel.view.get("path");
                if (oldPath !== panelStatus.path) {
                    log.log("Setting new path: " + oldPath + " != " + panelStatus.path);
                    var panelView: toDoView.PageView = <toDoView.PageView> panelStatus.panel.view;
                    var pathParts: string[] = Y.Array.filter(panelStatus.path.split(/\/+/), function (it) { return it; });
                    pathParts.shift(); // shift off the initial header "ToDo" prefix thing
                    var assetPath: string = "active/" + pathParts.join("/active/");

                    log.log("Loading ToDo's under " + assetPath);
                    var dataPromise = this.todoTool.getUserDataFolder().then(
                            (ref: axMgr.AssetRef) => {
                                if (pathParts.length > 0) {
                                    return this.assetTool.loadSubpath(ref.getAsset().getId(), assetPath);
                                } else {
                                    return Y.when( ref );
                                }
                            }
                        ).then(
                            (ref: axMgr.AssetRef) => {
                                if (ref.isDefined()) {
                                    log.log("Loading ToDo's under " + ref.getAsset().getId());
                                    return this.todoTool.loadActiveToDos(ref.getAsset().getId()
                                        ).then(
                                            (children) => {
                                                return new toDoView.ViewModel(ref, children, panelStatus.path, ref.getAsset().getComment() );
                                            }
                                       );
                                } else {
                                    log.log("No asset found at " + panelStatus.path);
                                    return Y.when( new toDoView.ViewModel(ref, [], panelStatus.path, "no data found" ) );
                                }
                            }
                        );
                    panelView.setModelPromise(dataPromise).then(
                        (data) => {
                            this.app.markPanelDirty(panelStatus.panel.id);
                        },
                        (err) => {
                            panelView.setModel(new toDoView.ViewModel(axMgr.emptyRef(), [], panelStatus.path, "Error: " + err));
                            this.app.markPanelDirty(panelStatus.panel.id);
                        }
                     );
                    this.app.markPanelDirty(panelStatus.panel.id);
                    
                }
            }
        };
    }


    /**
     * toDoApp config (passed to start())
     * @class Config
     */
    export class Config {
        public container: string = "div#app";
        public root: string = "/littleware_apps/testsuite/littleToDoTestSuite.html";
    }

    /**
     * Configure the toDo application.  
     * @method buildApp
     * @param config {Config} runtime config
     * @return {littleApp.ViewManager.Manager} ready for app.show()
     */
    export function buildApp( config?:Config ): littleApp.ViewManager.Manager {
        // home page info
        var config = config || new Config();
        var versionInfo = Y.Node.create("<div id='app-info'><span class='app-title'>Little To Do</span><br>Version 0.0 2013/08/21</p></div>");
        var homePage:Y.View = new Y.View();
        homePage.get("container").append(versionInfo);

        // router
        var router:Y.Router = new Y.Router(
            { root:  config.root }
            );

        // inject homepage and router into view manager
        var app = littleApp.ViewManager.getFactory().create("app", config.container, homePage, router);

        // create a couple root ToDo's if none exist
        var todoMgr = toDo.getToDoManager();
        todoMgr.getUserDataFolder().then(
            (ref) => {
                return todoMgr.loadActiveToDos(ref.getAsset().getId()).then(
                    (todos: toDo.ToDoSummary[]) => {
                        if (todos.length < 1) {
                            return Y.batch(
                                todoMgr.createToDo(ref.getAsset().getId(), "Groceries", "grocery list"),
                                todoMgr.createToDo(ref.getAsset().getId(), "ToDo", "grocery list")
                            );
                        } else {
                            return Y.when(todos);
                        }
                    }
                );
            }
        ).then(
            (todos) => {
                // register the todo root
                return toDoView.PageViewFactory.newView();
            }
        ).then(
            (view) => {
                var toDoRoot = new littleApp.LittlePanel("ToDo", view);
                var controller = new AppController( todoMgr, app, axMgr.getAssetManager());
                app.registerRootPanel(toDoRoot, "ToDo", controller.viewListener.bind(controller));
            },
            (err) => {
                alert("Failed ToDo setup: " + err);
            }
        );
        return app;
    }


    log.log("module initialized ...");
}
