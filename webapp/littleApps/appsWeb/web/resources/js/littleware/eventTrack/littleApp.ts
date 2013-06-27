/// <reference path="../../libts/yui.d.ts" />
declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-eventTrack-littleApp";
}


var Y:Y.YUI = exports;
var lw: any = exports.littleware;

export module littleware.eventTrack.littleApp {
    var log = new lw.littleUtil.Logger("littleware.eventTrack.littleApp");
    log.log("littleware logger loaded ...");

    /**
     * Base class for enumertions - see PanelState below
     */
    export class LittleEnum {
        constructor(public id: number, public name: String) { }
        toString(): String { return name; }
    }

    /**
     * Interface for panel managed by ViewManager.Manager.
     * The id should be unique across panel instances.
     */
    export class LittlePanel {
        constructor(
            public id: string,
            public view: Y.View
            ) {
            Y.assert(id ? true : false, "Id must evaluate true in a boolean expression: " + id);
            }
    }

    export class PanelState extends LittleEnum {
        constructor(name: String) { super(PanelState.values.push( this ), name); }

        static values:PanelState[] = [];
        static VISIBLE: PanelState = new PanelState( "VISIBLE");
        static HIDDEN:PanelState = new PanelState(  "HIDDEN" );
    }
    

    /**
     * Data supplied to a panel's view-state listener 
     * (supplied to ViewManager.registerPanel) when
     * the managed view moves to a new route path.
     */
    export class PanelStatus {
        constructor(
            public panel: LittlePanel,
            public state: PanelState,
            public path:string
            ) { }
    }



    /**
     * ViewManager attempts to manage the visible "panels"
     * in browser-like apps where a user navigates from a parent
     * view to different child views - the parent's panel appearing
     * on the left of the screen, and the child to the parent's right.
     * Depending on the screen size, the manager may render
     * the single panel that currently has the user focus (on a phone),
     * two panels (a parent and one child on a tablet), or
     * three to four (?) panels on a full computer screen.
     * So the app moves between different pages, where each page
     * has a root panel with zero or more generations of children.
     *
     * @namespace littleware.eventTrack.littleApp.ViewManager
     */
    export module ViewManager {

        /**
         * Enumeration for ViewManager life cycle state machine.
         *
         * @class ManagerState
         */
        class ManagerState extends LittleEnum {

            constructor(name) {
                super( ManagerState.values.push( this ), name);
            }

            static values:ManagerState[] = [];
            static NEW = new ManagerState("NEW");
            static ACTIVE = new ManagerState( "ACTIVE" );
        }

        /**
         * Enumeration track how many panels the view should
         * display based on the screen width.
         *
         * @class ViewMode
         */
        class ViewMode extends LittleEnum {
            constructor(name) {
                super(ViewMode.values.push(this), name);
            }
            
            static values: ViewMode[] = [];

            /**
             * @static
             * @property ONEPANEL
             */
            static ONEPANEL = new ViewMode("ONEPANEL"); // phone mode
            static TWOPANEL = new ViewMode("TWOPANEL"); // tablet mode
            static THREEPANEL = new ViewMode("THREEPANEL"); // laptop
            static FOURPANEL = new ViewMode("FOURPANEL"); // big screen

            /**
             * @static
             * @method selectModeByWidth
             * @param width {int}
             */
            static selectModeByWidth(width: number): ViewMode {
                var mode = FOURPANEL;

                if (width < 1101) {
                    mode = THREEPANEL;
                }
                if (width < 801) {
                    mode = TWOPANEL;
                }
                if (width < 501) {
                    mode = ONEPANEL;
                }
                return mode;
            }
        }


        /**
         * Little helper manages the display of panels based
         * on the routes triggered in router and size of display
         * (phone, tablet, whatever).
         * Assumes tree-based panel app.
         * Currently limits to at most 20 panels, 6 panel depth
         *
         * @class Manager
         */
        export interface Manager {
            name: string;

            /**
             * Root div under which to manage the panel UI
             */
            container: Y.Node;

            /**
             * Info panel embedded in home page at path "/" - splash screen,
             * version info, whatever placed above the "route index"
             * @property homePage {Y.View}
             */
            homePage: Y.View;
            router: Y.Router;

            /**
             * List of routes to sort and display in the "index" on the home page
             * along with the "root" panel paths.
             */
            routeIndex: string[];


            /**
             * Child panel of homePage - "config"
             * is reserved for internally managed configuration panels.
             */
            registerRootPanel(
                panel: LittlePanel,
                baseName: string,
                listener: (PanelStatus) => void
                );

            /**
             * Register panels along with id of its parent, and a baseFilter
             * that either accepts or rejects a route basename.
             * For example, given some route /path/to/parent/bla/foo/frick,
             * then for each element of the path [path,to,parent,bla,foo,frick],
             * test that element against the children of a parent panel
             * to determine which panel to associate with that route.
             *
             * @method registerPanel
             */
            registerPanel(
                    panel: LittlePanel,
                    parentId: string,
                    routeFilter: (string) => bool,
                    listener: (PanelStatus) => void
                );

            /**
             * By default the manager does not re-render a panel when it
             * becomes "visible" unless the panel is "dirty".
             * If the panel is already visible, then re-render panel once
             * call stack clears: Y.later( 0, () => render() ).
             *
             * @method markPanelDirty
             */
            markPanelDirty(panelId: string);

            /**
             * Triggers the manager to render its initial view (depends on the active route),
             * and begin responding to routing and dirty-panel notifications in the "Active" 
             * ManagerState.  NOOP if already active.
             *
             * @method show
             */
            show();

        }


        export interface Factory {
            /**
             * Create a new view manager that manipulates DOM in the given selector
             *
             * @param name alphanumeric to associate with this manager - used as a key
             *               in persistence store
             * @param selector CSS selector for div under which to build view
             */
            create(name: string, selector: string, homePage: Y.View, router: Y.Router): Manager;
            //create( name:string, selector: string): Manager;

            /**
             * Load manager state from persistent storage
             */
            load( name:string ): Manager;
        }


        module internal {
            var homeId = "littleHome"

            class PanelInfo {
                constructor(
                    public panel: LittlePanel,
                    public routeFilter:(string) => bool,
                    public listener: (PanelStatus) => void
                    ) {
                    if (null == listener) { this.listener = function () { } };
                    }
            }

            /**
             * LittlePanel where view is a stacked splashView and indexView
             *
             * @class HomePanel
             */
            class HomePanel extends LittlePanel {
                splashView: Y.View;
                indexView: Y.View;

                /**
                 * Constructor combines splashView and indexView into a compound view.
                 * The client is responsible for invoking render() on splashView at
                 * some point - the compound view does not re-render the splash.
                 */
                constructor( splashView: Y.View, indexView: Y.View) {
                    var compoundView = new Y.View();
                    var container:Y.Node = compoundView.get("container");
                    container.append(splashView.get("container"));
                    container.append(indexView.get("container"));
                    compoundView.render = function () { indexView.render(); return this; }

                    super(homeId, compoundView);
                }

            }

            class ViewInfo {
                panelId: string;

                constructor(public div: Y.Node) { this.panelId = null; }
            }

            export class ManagerImpl implements Manager {
                private homePanel: HomePanel;
                // link index on the home page below the app-specific intro view
                private indexView:Y.View = new Y.View();
                private state = ManagerState.NEW;
                // active panels based on the current router route
                private routePanelIds: string[] = [];
                // 3 view panel divs with the LittlePanel currently being displayed (if any)
                private viewInfo:ViewInfo[] = [ null, null, null, null ];

                routeIndex: string[] = [];

                /**
                 * Id to panel map
                 */
                private panelIdIndex: {
                    [id: string]: PanelInfo;
                } = {};

                private viewMode = window ? ViewMode.selectModeByWidth( window.innerWidth ) : ViewMode.THREEPANEL;
                /**
                 * Parent panel id to children list
                 */
                private parentIdToChildren: { [id: string]: string[]; } = {};

                getViewMode(): ViewMode { return this.viewMode; }


                constructor(
                    public name: string,
                    public container: Y.Node,
                    public homePage: Y.View,
                    public router:Y.Router
                    ) {
                    var mgr = this;
                    this.indexView.render = function () {
                        // TODO - load template from somewhere
                        var index = mgr.routeIndex;
                        index.sort();
                        var content = "<ul class='pure-menu pure-menu-open'>\n";
                        for (var i = 0; i < index.length; ++i) {
                            content += "<li><a class='little-route' href='" + index[i] + "'>" + index[i] + "</a></li>";
                        }
                        content += "</ul>";
                        log.log("Rendering index view: " + content);
                        this.get("container").setHTML(content);
                        return this;
                    };

                    this.homePanel = new HomePanel(homePage, this.indexView);
                    this.routePanelIds.push(this.homePanel.id);
                    this.panelIdIndex[this.homePanel.id] = new PanelInfo(this.homePanel, (s) => true, null);
                    this.markPanelDirty(this.homePanel.id );

                    var gnode = Y.Node.create("<div class='pure-g' />");
                    container.append(gnode);
                    for (var i = 0; i < 4; ++i) {
                        var node = Y.Node.create("<div class='pure-u little-panel little-panel" + i + "' />");
                        this.viewInfo[i] = new ViewInfo(node);
                        gnode.append(node);
                    }

                    router.route("*", (req) => {
                        var panelIds = this.lookupPanelsForRoute(req.path);
                        this.routePanelIds = panelIds;
                        this.scheduleRender();
                    }
                        );
                  }

                
                /**
                 * Determine the list of panel ids to display for the given route
                 *
                 * @param path sub-root route path: req.path
                 * @return {Array<String>} list of panel ids in left-to-right order
                 */
                private lookupPanelsForRoute(path:string) {
                    log.log("Handling route: " + path);
                    var parts: string[] = Y.Array.filter(path.split(/\/+/), function (s) { return s; });
                    var panelIds: string[] = [];
                    // always start at home
                    panelIds.push(homeId);
                    var parent = homeId;
                    for (var i = 0; (i < parts.length) && (parent != null); i++) {
                        var children:PanelInfo[] = Y.Array.filter(
                            Y.Array.map( this.parentIdToChildren[parent], (childId) => this.panelIdIndex[childId] ),
                            (info) => info.routeFilter( parts[i] )
                            );
                        if (children.length > 0) {
                            parent = children[0].panel.id;
                            panelIds.push(parent);
                        } else {
                            parent = null;
                            log.log("Failed to process full route for: " + path + ", failed at: " + parts[i]);
                        }
                    }
                    return panelIds;
                }
                



                registerRootPanel(
                    panel: LittlePanel,
                    baseName: string,
                    listener: (PanelStatus) => void
                    ) {
                    this.registerPanel(panel, homeId, (name) => name == baseName, listener);
                    this.routeIndex.push("/" + baseName);
                    this.markPanelDirty(this.homePanel.id);
                 }


                registerPanel(
                    panel: LittlePanel,
                    parentId: string,
                    baseFilter: (string) => bool,
                    listener: (PanelStatus) => void
                    ) {
                    Y.assert(Y.Lang.isUndefined(this.panelIdIndex[panel.id]),
                             "Panel id may only be registered once: " + panel.id
                             );
                    this.panelIdIndex[panel.id] =
                        new PanelInfo(panel, baseFilter, listener);
                    var siblings = this.parentIdToChildren[parentId];
                    if (Y.Lang.isUndefined(siblings)) {
                        siblings = [];
                        this.parentIdToChildren[parentId] = siblings;
                    }
                    siblings.push(panel.id);
                    this.markPanelDirty(panel.id);
                }


                private dirtyPanels: { [id: string]: bool; } = {};

                private renderPending = false;
                private scheduleRender() {
                    if ((!this.renderPending) && (this.state.id == ManagerState.ACTIVE.id) ) {
                        this.renderPending = true;
                        log.log("Scheduling render ...");
                        Y.soon(() => { try {
                            log.log("Render running for route: " + this.router.getPath() );
                            var renderPanels: LittlePanel[] = [];
                            var numToRender = 4;
                            if (this.viewMode.id == ViewMode.THREEPANEL.id) {
                                numToRender = 3;
                            }  else if (this.viewMode.id == ViewMode.TWOPANEL.id) {
                                numToRender = 2;
                            } else if (this.viewMode.id == ViewMode.ONEPANEL.id ) {
                                numToRender = 1;
                            }

                            var newPanelIds: string[] = [];
                            for (var i = this.routePanelIds.length; (i >= 0) && (newPanelIds.length < numToRender); i--) {
                                if (this.routePanelIds[i] != null) {
                                    newPanelIds.unshift(this.routePanelIds[i]);
                                }
                            }
                            log.log("Rendering panels: " + newPanelIds.join(", "));

                            var routerPath = this.router.getPath();

                            //
                            // first - go through and remove children, 
                            // so we don't accidentally try to give a DOM node 2 parents ...
                            //
                            for( var i=0; i < this.viewInfo.length; ++i ) {
                                  // reset each panel's content as necessary
                                var currentView = this.viewInfo[i];
                                Y.assert(currentView.div != null, "View div looks ok - ugh! " + i);

                                  if ( (i < numToRender) && (i < newPanelIds.length) ) {
                                      if ( (currentView.panelId) && (currentView.panelId != newPanelIds[i]) ) { // && info.div.hasChildNodes()) {
                                          var panelInfo = this.panelIdIndex[ currentView.panelId ];
                                          panelInfo.listener( new PanelStatus( panelInfo.panel, PanelState.HIDDEN, routerPath ) );
                                          currentView.div.setHTML("");
                                          currentView.panelId = null;
                                      }
                                    
                                      // CSS media queries manage this ... .info.div.setStyle("width", "" + (100 / numToRender) + "%");
                                      //info.div.show();
                                  } else if ( currentView.panelId ) {
                                      //node.setStyle("width", "0" );
                                      //if ( info.div.hasChildNodes()) {
                                          //info.div.get("children").each((child) => info.div.remove(child));
                                          
                                      //}
                                      var panelInfo = this.panelIdIndex[ currentView.panelId];
                                      panelInfo.listener(new PanelStatus(panelInfo.panel, PanelState.HIDDEN, routerPath ));

                                      currentView.div.setHTML("");
                                      //info.div.hide();
                                      currentView.panelId = null;
                                  }
                                  Y.assert(currentView.div != null, "View div looks ok - ugh!");
                            }

                            //
                            // now go through and place the panels to display at the current route,
                            // and notify the listeners since we're probably at a new path
                            //
                            for (var i = 0; (i < numToRender) && (i < newPanelIds.length); i++) {
                                var currentView = this.viewInfo[i];
                                var newPanelId = newPanelIds[i];
                                var newPanelInfo = this.panelIdIndex[newPanelId];
                                //
                                // notify new panel that it's visible, so it can update itself, and mark itself dirty for render
                                // if necessary.  The render() call is made further below ...
                                //
                                newPanelInfo.listener(new PanelStatus(newPanelInfo.panel, PanelState.VISIBLE, routerPath ));

                                if (currentView.panelId != newPanelId ) {
                                    Y.assert(!Y.Lang.isUndefined(newPanelInfo), "Panel registered for display exists: " + newPanelId);
                                    Y.assert(!currentView.div.hasChildNodes(), "Panel div should be clear of children");
                                    currentView.panelId = newPanelId;
                                    currentView.div.append(newPanelInfo.panel.view.get("container"));
                                }
                                Y.assert(currentView.panelId == newPanelId, "Data looks consistent");
                                Y.assert(currentView.div != null, "View div looks ok - ugh!");

                                log.log("Checking if panel is dirty: " + newPanelId + ": " + this.dirtyPanels[newPanelId] );
                                // finally - re-render the panel if it's dirty
                                if (this.dirtyPanels[newPanelId]) {
                                    delete this.dirtyPanels[newPanelId];
                                    this.panelIdIndex[newPanelId].panel.view.render();
                                }
                            }
                        } finally { this.renderPending = false; }
                      });
                    }
                }

                markPanelDirty(panelId: string) {
                    this.dirtyPanels[panelId] = true;
                    // schedule a render if the panel is visible ...
                    this.scheduleRender();
                }

                show() {
                    if (this.state.id != ManagerState.ACTIVE.id) {
                        this.state = ManagerState.ACTIVE;
                        Y.on('windowresize', (ev) => {
                            this.viewMode = ViewMode.selectModeByWidth(window.innerWidth);
                            this.scheduleRender();
                        });
                        // register "a" click delegate to trigger routes when appropriate
                        var router = this.router;

                        this.container.delegate('click',
                            (e) => {
                                // Allow the native behavior on middle/right-click, or when Ctrl or Command
                                // are pressed.
                                if (e.button !== 1 || e.ctrlKey || e.metaKey) {
                                    return;
                                }

                                //
                                // Try to deal with relative paths in a reasonable way.
                                // The .href/get( 'href' ) absolute-path values are relative
                                // to the actual page URL, not the HTML5 pushState path ... ugh.
                                //
                                //log.log("Removing root from click target: " + e.currentTarget.get('href') + " -- " + e.currentTarget.getDOMNode().href);
                                var path: string = router.removeRoot( e.currentTarget.get('href') );
                                e.preventDefault();

                                if ( (router.getPath() != path) && router.hasRoute(path)) {    
                                    router.save(path);
                                }
                            }, 'a.little-route'
                          );

                        this.scheduleRender();
                    }
                }

            }
        }

        var factorySingleton: Factory = {
            
            create: function (name: string, selector: string, homePage:Y.View, router:Y.Router ): Manager {
                /*
                public name: string,
public container: Y.Node,
public homePage: Y.View,
public router: Y.Router
    */
                return new internal.ManagerImpl(name, Y.one(selector), homePage, router);
          },
          
            load: function (name: string): Manager { throw new Error("Not yet implemented");  return null; }
        };

        export function getFactory(): Factory { return factorySingleton; }


        //----------------------------------------

        export module TestSuite {
        }

    }

    
}