declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-auth-authView";
}

import importY = require("../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;

import importAuth = require("authService");
importAuth;
import authService = importAuth.littleware.auth.authService;

import importLittleApp = require("../eventTrack/littleApp");
importLittleApp;
import littleApp = importLittleApp.littleware.eventTrack.littleApp;


// lw for accessing untyped littleware modules implements in javascript
var lw: any = exports.littleware;
var littleId: any = lw.auth.littleId;

/**
 * @module littleware-auth-authView
 * @namespace littleware.auth.authView
 */          
export module littleware.auth.authView {
    
    var log = new lw.littleUtil.Logger("littleware.auth.authView");
    log.log("littleware logger loaded ...");


    /**
     * Interface of the auth PageView that shows the current authentication state (auth.SessionInfo),
     * and offers controls to login with openId, logout, etc.
     * @class PageView
     */
    export interface PageView extends Y.View {
        helper: authService.AuthManager;

        /**
         * Just a shortcut for authManager.getSessionInfo()
         * @method getSessionInfo
         * @return {SessionInfo}
         */
        getSessionInfo(): authService.SessionInfo;
    }

    
    var templatePath = "/littleware_apps/resources/templates/littleware/auth/authPanel.handlebars"
    log.log("Loading template: " + templatePath );
    var templatePromise:Y.Promise<(any) => string> = lw.littleUtil.loadHandlebar( templatePath
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
            var helper: authService.AuthManager = this.helper;
            var session = helper.getSessionInfo();
            //console.dir(attrs);
            
            var content:string = "uninitialized";
            var templateData = session;
            var content:string = this.template(templateData);

            container.setHTML(content);
            return this;
        },

    }, {
        ATTRS: {
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
        newView: function (): Y.Promise<PageView> {
            return templatePromise.then((template) => {
                var view = new SimplePageView();
                var helper = authService.Factory.get();
                view.template = template;
                view.helper = helper;
                return view;
            });
        }
    };

    //-----------------------------------

    /**
     * Controller attached to view by decorateApp
     * @class Controller
     */
    export class Controller {
        private authHelper: authService.AuthManager;

        /**
         * @constructor
         * @param view {PageView} panel wrapping a PageView
         * @param app {ViewManager.Manager} application manager
         * @param panelId {string} the id of the panel containing view registered with app
         */
        constructor(
            private view: PageView,
            private app: littleApp.ViewManager.Manager,
            private panelId: string
            ) {
            this.authHelper = this.view.helper;
        }

        /**
         * Click handler to delegate Login/Logout click handling to
         * @method handleLoginLogout
         */
        handleLoginLogout(ev: Y.DOMEventFacade): void {
            var command: string = ev.currentTarget.getAttribute("data-action");

            if (command == "logout") {
                this.authHelper.logout();
            } else {
                window.open("/littleware_apps/auth/openIdPop.html", "openid_popup");
            }
        }

        /**
         * Mark the panel dirty when the login state changes
         * @method handleStateChange
         */
        handleStateChange(ev): void {
            this.app.markPanelDirty(this.panelId);
        }
    }

    //-----------------------------------


    /**
     * Add a login panel to the root of the app managed by the given view manager.
     * The login panel encompasses and AuthView with a controller that udpates its
     * view when the auth state changes, and provides login/logout capability.
     *
     * @method decorateApp
     * @for authView
     * @return {Y.Promise<littleApp.ViewManager.Manager>}
     */
    export function decorateApp(app: littleApp.ViewManager.Manager): Y.Promise<littleApp.ViewManager.Manager> {
        var batch: Y.Promise<littleApp.ViewManager.Manager> = Y.batch( PageViewFactory.newView(), littleId.helperFactory.get() ).then(
            (args) => {
                var view: PageView = args[0];
                var littleIdHelper = args[1];

                // wrap the view in a panel
                var rootPanel = new littleApp.LittlePanel("Login", view);
                // attach a controller to the view
                var controller = new Controller(view, app, rootPanel.id);
                view.get("container").delegate(
                    "click", controller.handleLoginLogout.bind(controller), "a.little-tools"
                    );
                view.helper.attrs.after("sessionInfoChange", (ev) => {
                    controller.handleStateChange(ev);
                });
                littleIdHelper.after("userCredsChange",
                    (ev) => {
                        var creds = littleIdHelper.get("userCreds");
                        if (creds && creds.email && creds.secret) {
                            view.helper.authenticate(creds.email, creds.secret);
                        }
                    }
                 );
                // register the panel with the app
                app.registerRootPanel(rootPanel, "Login", () => { });
                return app;
            }
            );

        return batch;
    }

    //-----------------------------------

    /**
     * @method buildTestSuite
     * @static
     */
    export function buildTestSuite(): Y.Test_TestSuite {
        var suite: Y.Test_TestSuite = new Y.Test.TestSuite("littleware-authView");
        suite.add(new Y.Test.TestCase(
                {
                    // 
                    // just try to render a view into the current document
                    //
                    testAuthView: function () {
                        decorateApp(new littleApp.ViewManager.MockManager()).then(
                            (app: littleApp.ViewManager.Manager) => {
                                this.resume(() => {
                                    var panel = app.lookupPanelById("Login");
                                    Y.Assert.isTrue(panel && true, "Login panel registered with app");
                                    Y.Assert.isTrue( panel.view.template && true, "View looks initialized");
                                });
                            },
                            (err) => {
                                this.resume(() => {
                                    Y.Assert.fail("Failed to initialize view: " + err);
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
