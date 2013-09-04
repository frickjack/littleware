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

        private panelId2AssetId: { [key: string]: string; } = {};

        /**
         * Little helper - loads the model associated with a view
         * @method loadModel
         * @param id {string} of root ToDo
         * @param route {string} route associated with this id
         * @return {Promise{ViewModel}} Promise for ViewModel with undefined path
         */
        loadModel(id: string, route:string ): Y.Promise {
            return this.assetTool.loadAsset( id 
            ).then(
                (ref: axMgr.AssetRef) => {
                    if (ref.isDefined()) {
                        log.log("Loading ToDo's under " + ref.getAsset().getId());
                        return this.todoTool.loadActiveToDos(ref.getAsset().getId()
                            ).then(
                                (children) => {
                                    return new toDoView.ViewModel(ref, children, route, ref.getAsset().getComment());
                                }
                           );
                    } else {
                        log.log("No asset found at " + route);
                        return Y.when(new toDoView.ViewModel(ref, [], route, "no data found"));
                    }
                }
            );

        }

        /**
         * Update the model and mark dirty the view associated with the given asset id
         * if any view is associated with that id.  If the asset is a ToDo, then
         * recurse on the parentToDoId.  May run asynchronously.
         * @method updateAssetView
         * @param assetId {string}
         */
        updateViews(assetId: string): void {
            this.assetTool.loadAsset( assetId ).then(
                (ref: axMgr.AssetRef) => {
                    if (ref.isDefined()) {
                        var panelId: string = null;
                        for (var id in this.panelId2AssetId) {
                            if (this.panelId2AssetId[id] === assetId) {
                                panelId = id;
                                break;
                            }
                        }

                        if (panelId) {
                            var panel: littleApp.LittlePanel = this.app.lookupPanelById(panelId);
                            // update the panel's model
                            if (panel) {
                                var view = <toDoView.PageView> panel.view;
                                view.setModelPromise(this.loadModel(assetId, view.get("path"))
                                    ).then(
                                        (data) => {
                                            this.app.markPanelDirty(panelId);
                                        }
                                    );
                            }
                        } else {
                            log.log("No panel found for asset id: " + assetId);
                        }

                        // if ToDo asset has changed, then update its parent view too
                        if (ref.getAsset().getAssetType().id === toDo.ToDoItem.TODO_TYPE.id) {
                            var parentId: string = (<toDo.ToDoItem> ref.getAsset()).getParentToDoId();
                            if (parentId) {
                                this.updateViews(parentId);
                            }
                        }
                    }
                }
             );

        }


        /**
         * Listener passed to app.registerPanel at app setup time - 
         *  updates panel path and ToDo data model of new path on view change.
         * @method viewListener
         * @param panelStatus {PanelStatus}
         */
        viewListener(panelStatus: littleApp.PanelStatus): void {
            console.log("viewListener considering: " + panelStatus.panel.id);
            console.dir(panelStatus);
            var oldPath = panelStatus.panel.view.get("path") || "";

            if ( (panelStatus.state.name === "VISIBLE") && (oldPath != panelStatus.panelPath) ) {
                
                //var ref:axMgr.AssetRef = panelStatus.panel.view.get("asset") || axMgr.emptyRef();
                log.log("Setting new path: " + oldPath + " != " + panelStatus.panelPath);
                console.dir(panelStatus);
                var panelView: toDoView.PageView = <toDoView.PageView> panelStatus.panel.view;
                var pathParts: string[] = Y.Array.filter(panelStatus.panelPath.split(/\/+/), function (it) { return it; });
                pathParts.shift(); // shift off the initial header "ToDo" prefix thing
                var assetPath: string = "active/" + pathParts.join("/active/");

                log.log("Loading ToDo's under " + assetPath);
                var dataPromise = this.todoTool.getUserDataFolder().then(
                        (ref: axMgr.AssetRef) => {
                            Y.assert(ref.isDefined(), "User data folder should be auto-setup");
                            if (pathParts.length > 0) {
                                return this.assetTool.loadSubpath(ref.getAsset().getId(), assetPath);
                            } else {
                                return Y.when( ref );
                            }
                        }
                    ).then(
                        (ref: axMgr.AssetRef) => {
                            if (ref.isDefined()) {
                                return this.loadModel(ref.getAsset().getId(), panelStatus.panelPath );
                            } else {
                                log.log("No asset found at " + panelStatus.panelPath );
                                return Y.when( new toDoView.ViewModel(ref, [], panelStatus.panelPath, "no data found" ) );
                            }
                        }
                    );
                panelView.setModelPromise(dataPromise).then(
                    (data: toDoView.ViewModel) => {
                        if (data.asset.isDefined()) {
                            this.panelId2AssetId[panelStatus.panel.id] = data.asset.getAsset().getId();
                        } else {
                            delete this.panelId2AssetId[panelStatus.panel.id];
                        }
                        this.app.markPanelDirty(panelStatus.panel.id);
                    },
                    (err) => {
                        panelView.setModel(new toDoView.ViewModel(axMgr.emptyRef(), [], panelStatus.panelPath, "Error: " + err));
                        this.app.markPanelDirty(panelStatus.panel.id);
                    }
                    );
                this.app.markPanelDirty(panelStatus.panel.id);
            }
        }


        /**
         * Key listener on input text fields manages creation of 
         * new ToDo's when user hits "RETURN" key or whatever.
         * We assume that the input-text fields has the data-panel-path
         * attribute set.
         * @method newToDoListener
         * @param ev {Event} keyboard event with text-field target
         */
        uxNewToDoListener(ev: Y.DOMEventFacade): void {
            var target: Y.Node = ev.currentTarget;
            var content: string = target.get("value");
            if (!content) {
                return;
            }
            log.log("Adding new ToDo: " + content);
            
            var panelPath: string = ev.currentTarget.getAttribute("data-panel-path");
            var panels: littleApp.LittlePanel[] = this.app.lookupPanels(panelPath);
            var panel: littleApp.LittlePanel = panels[panels.length - 1];

            if (panel && panel.view) {
                var view: toDoView.PageView = <toDoView.PageView> panel.view;
                var model = view.getModel();
                if (model.asset && model.asset.isDefined()) {
                    var parentId: string = model.asset.getAsset().getId();
                    this.todoTool.createToDo(parentId, content, "new ToDo");
                }
                // keep focus on the input, so the user can add another ToDo!
                target.focus();
            } else {
                log.log("Panel does not hold a view ?");
                console.dir(panel);
            }
        }

        /**
         * Delete-button listener launches todo-delete/archive workflow
         * for selected (by checkbox) assets when the delete button is pressed.
         * @method uxDeleteListener
         * @param ev {Event} click event with delete-button a-link target
         */
        uxDeleteListener(ev: Y.DOMEventFacade): void {
            var div: Y.Node = ev.currentTarget.ancestor("div");
            var checks: Y.NodeList = div.all( "input[type='checkbox']" );
            var ids: string[] = [];
            checks.each(
                (node) => {
                    if (node.get("checked")) {
                        ids.push(node.get("name"));
                    }
                }
             );
            log.log("Archiving ToDos: " + ids.join(","));
            Y.Array.each( ids,
                (id) => {
                    this.todoTool.updateToDo(id, { state: toDo.ToDoItem.STATE.COMPLETE });
                }
              );
        }

        /**
         * Listener for new asset events, etc. - listens for events from
         * the asset repository.
         * @method assetListener
         * @param ev {axMgr.RefEvent} event data
         */
        assetListener(ev: axMgr.RefEvent): void {
            this.assetTool.loadAsset(ev.id).then(
                (ref: axMgr.AssetRef) => {
                    if (ref.isDefined() &&
                        (ref.getAsset().getAssetType().id === toDo.ToDoItem.TODO_TYPE.id)
                        ) {
                        // ToDo asset has changed - update the ToDo's view, and its parent views too
                        this.updateViews(ref.getAsset().getId());
                    }
                }
             );
        }
    }


    /**
     * toDoApp config (passed to start())
     * @class Config
     */
    export class Config {
        public container: string = "div#app";
        public root: string = "/littleware_apps/testsuite/littleToDoTestSuite.html";
    }

    // arbitrary limit on ToDo nesting for now
    var maxToDoDepth = 6;

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
                            //
                            // do not "batch" these operations - they're not parallel (async-step) safe,
                            // since both operate on the same part of the asset tree
                            //
                            var parentId = ref.getAsset().getId();
                            return todoMgr.createToDo(parentId, "Groceries", "grocery list"
                                ).then(
                                  (todo1) => {
                                    return todoMgr.createToDo(parentId, "ToDo", "ToDos"
                                        ).then((todo2) => {
                                            return [todo1, todo2];
                                        }
                                    );
                                }
                              );
                        } else {
                            return Y.when(todos);
                        }
                    }
                );
            }
        ).then(
            (todos) => {
                var views: Y.Promise[] = [];
                for (var i = 0; i < maxToDoDepth; ++i) {
                    views.push(toDoView.PageViewFactory.newView());
                }
                // register the todo root
                return Y.batch.apply( Y, views );
            }
        ).then(
            (views: toDoView.PageView[]) => {
                Y.assert(views.length === maxToDoDepth, "Views length looks ok: " + maxToDoDepth );
                var rootPanel = new littleApp.LittlePanel("ToDo", views[0]);
                var assetMgr = axMgr.getAssetManager();
                var controller = new AppController(todoMgr, app, assetMgr );
                var navListener = controller.viewListener.bind(controller);
                app.registerRootPanel(rootPanel, "ToDo", navListener );
                var panels: littleApp.LittlePanel[] = [rootPanel];
                var i = 0;

                for ( i = 1; i < views.length; ++i) {
                    var newPanel = new littleApp.LittlePanel("ToDo" + i, views[i]);
                    panels.push(newPanel);
                    app.registerPanel(newPanel, panels[i - 1].id, function (s) { return true; }, navListener);
                }
                for (i = 0; i < views.length; ++i) {
                    // register event-handler for new toDo text fields
                    views[i].get("container").delegate(
                            "key", controller.uxNewToDoListener.bind(controller), "enter", "input"
                        );
                    views[i].get("container").delegate(
                            "click", controller.uxDeleteListener.bind(controller), "a[data-action='delete']"
                        );
                }

                // register listener for model changes
                assetMgr.addListener(controller.assetListener.bind(controller));
            },
            (err) => {
                log.log("Failed setup: " + err);
                alert("Failed ToDo setup: " + err);
            }
        );
        return app;
    }


    log.log("module initialized ...");
}
