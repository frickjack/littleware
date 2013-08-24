/// <reference path="../../libts/yui.d.ts" />


declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-eventTrack-toDoView";
}

// little dance for hooking into YUI's module system
var Y: Y = exports;


// lw for accessing untyped littleware modules implements in javascript
var lw = exports.littleware;
import importLittleAsset = module("../asset/littleAsset");
import importAssetMgr = module("../asset/assetMgr");
import importToDo = module( "toDoAPI" );

import ax = importLittleAsset.littleware.asset;
import axMgr = importAssetMgr.littleware.asset.manager;
import toDo = importToDo.littleware.eventTrack.toDoAPI;

/**
 * @module littleware-eventTrack-toDoView
 * @namespace littleware.eventTrack.toDoView
 */          
export module littleware.eventTrack.toDoView {
    
    var log = new lw.littleUtil.Logger("littleware.eventTrack.toDoView");
    log.log("littleware logger loaded ...");


    /**
     * Data bucket summarizes the data attributes associated with the PageView (below)
     * @class ViewModel
     */
    export class ViewModel {
        constructor(
            public asset: axMgr.AssetRef,
            public children: toDo.ToDoSummary[],
            public path: string,
            public message: string
            ) { }
    }

    /**
     * Interface of the ToDo PageView that lists the ToDo children
     * under a parent
     * @class PageView
     */
    export interface PageView extends Y.View {
        /**
         * Get the state ('loading' or 'ready')
         * @method getState
         * @return {string} 'loading' or 'ready'
         */
        getState(): string;

        /**
         * Update the view state to point at a new asset
         * @method setAsset
         * @param attrs { asset:AssetRef, children:ToDoSummary[], path:string }
         */
        setModel(attrs: ViewModel ): void;

        /**
         * Register a promise that returns the setModel attributes on completion,
         * and manages the setModel call and transition into and out of a "loading" state.
         * Returns the promise that updates the model passing through the attributes object - the caller
         * can mark the parent LittlePanel dirty to re-render on promise completion.
         * @method setModelPromise
         * @param promise {Promise{ViewModel}}
         * @return {Promise} 
         */
        setModelPromise(promise: Y.Promise): Y.Promise;
    }

    
    var templatePath = "/littleware_apps/resources/templates/littleware/eventTrack/toDoView/toDoPanel.handlebars"
    log.log("Loading template: " + templatePath );
    var templatePromise:Y.Promise = lw.littleUtil.loadHandlebar( templatePath
        ).then(
            (tplate) => {
                log.log("Loaded template ...");
                return tplate;
            },
            (err) => {
                log.log("Failed to load template: " + err);
            }
        );

    var SimplePageView:any = Y.Base.create("pageView", Y.View, [], {

        /**
         * Redraw the content of the panel
         * @method render
         * @chainable
         */
        render: function () {
            var container: Y.Node = this.get('container');
            var attrs = this.getAttrs(["asset", "children", "path", "state"]);
            //console.dir(attrs);
            
            var content:string = "uninitialized";
            if (attrs.state === "loading") {
                content = "Loading ...";
            }  else if (attrs.asset && attrs.asset.isDefined()) {
                var templateData = {
                    label: attrs.asset.getAsset().getName(),
                    description: attrs.asset.getAsset().getComment(),
                    path: attrs.path,
                    children: []
                };

                var child: toDo.ToDoSummary = null;
                for (var i = 0; i < attrs.children.length; ++i) {
                    child = attrs.children[i];
                    templateData.children.push(
                            {
                                name: child.getLabel(),
                                numChildren: child.children.size()
                            }
                        );
                }
                content = this.template(templateData);
            } else {
                content = "<h1>No Data Found</h1>";
            }
            container.setHTML(content);
            return this;
        },

       setModel: function (attrs: ViewModel) {
            this.setAttrs(
                    {
                        asset: attrs.asset,
                        children: attrs.children,
                        path: attrs.path,
                        state: "ready"
                    }
                );
        },

        setModelPromise: function (promise: Y.Promise): Y.Promise {
            Y.assert(this.get("state") === "ready", "cannot set promise on view already in loading state");
            this.set("state", "loading");
            return promise.then(
                (attrs:ViewModel) => {
                    this.setModel(attrs);
                    return attrs;
                },
                (err) => {
                    this.set("state", "ready");
                    throw err;
                }
             );
        }
    }, {
        ATTRS: {
            /**
             * @attribute asset
             * @type AssetRef
             */   
            asset: { value: null },
            /**
             * @attribute children
             * @type ToDoSummary[]
             */
            children: { value: [] },
            /**
             * @attribute path
             * @type string
             */
            path: { value: "" },
            /**
             * @attribute state
             * @type string
             */
            state: { value: "ready" }
        }
    });

    /**
     * Factory for PageView instances - maries YUI class inheritance with Typescript type system
     * @class PageViewFactory
     */
    export var PageViewFactory = {
        /**
         * Returns a Promise - the view loads various configuration 
         * information (templates, whatever) at the template, and 
         * fulfills the promise when the data is ready.
         * @method newView
         * @static
         * @return {Promise{PageView}}
         */
        newView: function (): Y.Promise {
            return templatePromise.then((template) => {
                var view = new SimplePageView();
                view.template = template;
                return view;
            });
        }
    };

    //-----------------------------------

    export function buildTestSuite(): Y.Test_TestSuite {
        var suite: Y.Test_TestSuite = new Y.Test.TestSuite("littleware-toDoView");
        suite.add(new Y.Test.TestCase(
                {
                    // 
                    // just try to render a view into the current document
                    //
                    testToDoView: function () {
                        PageViewFactory.newView().then(
                            (view) => {
                                this.resume(() => {
                                    Y.Assert.isTrue(view.template && true, "View looks initialized");
                                })
                            },
                            (err) => {
                                this.resume(() => {
                                    Y.Assert.fail("Failed to initiialize view: " + err);
                                });
                            }
                            );
                        this.wait(2000);
                    }
                }
            ));
        return suite;
    }


    log.log("module initialized ...");
}
