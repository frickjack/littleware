declare var exports:any;


if ( null == exports ) {
    // Hook to communicate out to YUI module system a YUI module-name for this typescript file
    throw "littleware-auth-authService";
}

import importY = require("../../libts/yui");
importY; // workaround for typescript bug: https://typescript.codeplex.com/workitem/1531
import Y = importY.Y;
Y = exports;


// lw for accessing untyped littleware modules implements in javascript
var lw: any = exports.littleware;


/**
 * @module littleware-auth-authService
 * @namespace littleware.auth.authService
 */          
export module littleware.auth.authService {
    
    var log = new lw.littleUtil.Logger("littleware.auth.authService");
    log.log("littleware logger loaded ...");


    /**
     * Generic credentials bundle with data required for
     * interacting with littleware's authentication service.
     * Properites should be treated as read-only by clients.
     *
     * @class Creds
     */
    export class Creds {
        constructor(
            private user: string,
            private token: string,
            private expires: Date
            ) { }

        getUser(): string {
            return this.user;
        }

        getToken(): string {
            return this.token;
        }

        getExpireDate(): Date {
            return this.expires;
        }

    }


    /**
     * Session id and possibly credentials if the session has been authenticated
     * @class SessionInfo
     */
    export class SessionInfo {
        constructor(private id: string, private creds: Creds) { }

        /**
         * @method getId 
         */
        getId(): string {
            return this.id;
        }

        /**
         * @method getCreds
         * @return {Creds} creds if authenticated, else null
         */
        getCreds(): Creds {
            return this.creds;
        }

    }


    /**
     * Singleton (acquire via Factory) for interacting with auth service
     * @class AuthManager
     */
    export interface AuthManager {
        /**
         * Attributes delegate with "sessionInfo" attribute auto-updated by manager
         *
         * @property attrs
         */
        attrs: Y.Base;

        /**
         * Shortcut for attrs.get( "sessionInfo" )
         * @method getSessionInfo
         */
        getSessionInfo(): SessionInfo;

        /**
         * Refresh the currently active session from the authentication service.
         * May return cached credentials if optForceCheck is not set true.
         * @method getCreds
         * @return {Promise{SessionInfo}} promise delivers session info
         */
        refreshSessionInfo(): Y.Promise<SessionInfo>;

        /**
         * Authenticate as the given user verified by the given secret
         * @method authenticate
         * @param user {string}
         * @param secret {string}
         * @return {Promise{Session}} promise errors out if authentication fails
         */
        authenticate(user: string, secret: string): Y.Promise<SessionInfo>;

        /**
         * Logout the currently authenticated session if necessary
         * @method logout
         * @return {Promise{SessionInfo}} 
         */
        logout(): Y.Promise<SessionInfo>;
    }

    // TODO - inject this at startup/configuration time
    var authServiceRoot = "https://littleware.herokuapp.com/littleware_services";
    //var authServiceRoot = "http://localhost:8080/littleware_services/";

    /**
     * Get the base url hosting the auth service
     * @method getServiceRoot
     * @return {string}
     */
    export function getServiceRoot(): string { return authServiceRoot; }
    /**
     * Set the base url hosting the auth service
     * @method setServiceRoot
     * @param root
     */
    export function setServiceRoot(root: string): void {
    }

    /**
     * Internal class for AuthManager attributes modeling -
     * extended with Y.Base below.
     */
    class AuthMgrAttrs {
        static NAME = "AuthMgrAttrs";
        static ATTRS = {
            sessionInfo: {
                readOnly: true,
                value: new SessionInfo( "unknown", null )
            }
        };

        constructor() {
            // Y.Base initialization hook
            (<any> AuthMgrAttrs).superclass.constructor.apply(this, {});
        }
    }

    Y.extend(AuthMgrAttrs, Y.Base, {});

    class SimpleAuthManager implements AuthManager {

        private sessionInfoPromise: Y.Promise<SessionInfo> = null;
        private loginPromise: Y.Promise<SessionInfo> = null;
        private logoutPromise: Y.Promise<SessionInfo> = null;

        /**
         * Internal helper to help serialize auth requests
         * @method promises
         */
        private promises(): Y.Promise<void> {
            var promises = Y.Array.filter([this.sessionInfoPromise, this.loginPromise, this.logoutPromise], (x) => {
                return x;
            });

            var voidPromise = new Y.Promise((resolve, reject) => {
                resolve(true);
            });
            if (promises.length > 0) {
                return Y.batch.apply(Y, promises);
            } else {
                return voidPromise;
            }
        }

        public attrs: Y.Base = <Y.Base> new AuthMgrAttrs();

        getSessionInfo(): SessionInfo {
            return <SessionInfo> this.attrs.get( "sessionInfo" );
        }

        refreshSessionInfo(): Y.Promise<SessionInfo> {
            return this.promises().then(() => {
                this.sessionInfoPromise = new Y.Promise(
                    (resolve, reject) => {
                        Y.io(authServiceRoot + "/auth/login", {
                            method: "GET",
                            xdr: { credentials: true },
                            on: {
                                complete: (id, ev) => {
                                    log.log("sessionInfo response");
                                    console.dir(ev);
                                    this.sessionInfoPromise = null;

                                    if (ev.status == 200) {
                                        var json = JSON.parse(ev.responseText);
                                        var creds: Creds = null;

                                        if (json.authToken) {
                                            creds = new Creds(json.user || "user", json.authToken, new Date(json.authExpires));
                                        }

                                        var session: SessionInfo = new SessionInfo(json.id, creds);
                                        this.attrs._set("sessionInfo", session);
                                        resolve(session);
                                    } else {
                                        reject(ev);
                                    }
                                }
                            }
                        });
                    }
                    );

                return this.sessionInfoPromise;
            });
        }


        

        authenticate(user: string, secret: string): Y.Promise<SessionInfo> {
            return this.promises().then(() => {
                var url = authServiceRoot + "/auth/login?action=login&user=" +
                    encodeURIComponent(user) +
                    "&password=" + encodeURIComponent(secret);
                this.loginPromise = new Y.Promise(
                    (resolve, reject) => {
                        Y.io(url, {
                            method: "GET",
                            xdr: { credentials: true },
                            on: {
                                complete: (id, ev) => {
                                    log.log("login response");
                                    console.dir(ev);
                                    if (ev.status == 200) {
                                        var json = JSON.parse(ev.responseText);
                                        var creds: Creds = null;
                                        if (json.authToken) {
                                            creds = new Creds(json.user || "user", json.authToken, new Date(json.authExpires));
                                        }

                                        var session: SessionInfo = new SessionInfo(json.id, creds);
                                        log.log("login session: ");
                                        console.dir(session);

                                        this.attrs._set("sessionInfo", session);
                                        resolve(session);
                                    } else {
                                        reject(ev);
                                    }
                                }
                            }
                        });
                    }
                    );


                return this.loginPromise;
            });
        }



        logout(): Y.Promise<SessionInfo> {
            return this.promises().then(() => {
                this.logoutPromise = new Y.Promise(
                    (resolve, reject) => {
                        Y.io(authServiceRoot + "/auth/login?action=logout", {
                            method: "GET",
                            xdr: { credentials: true },
                            on: {
                                complete: (id, ev) => {
                                    log.log("logout response");
                                    console.dir(ev);
                                    if (ev.status == 200) {
                                        var json = JSON.parse(ev.responseText);
                                        var creds: Creds = null;

                                        var session: SessionInfo = new SessionInfo(json.id, creds);
                                        this.attrs._set("sessionInfo", session);
                                        resolve(session);
                                    } else {
                                        reject(ev);
                                    }
                                }
                            }
                        });
                    }
                    );

                return this.logoutPromise;
            });
        }

    }


    var mgrSingleton: AuthManager = new SimpleAuthManager();
    // go ahead and initialize the session info with real data (bogus data set in constructor)
    mgrSingleton.refreshSessionInfo().then(() => { },
        /* auto-retry a few times - heroku might have shut down the dyno since we're just beta testing ... */
        (err) => {
            mgrSingleton.refreshSessionInfo().then(
                () => { }, (err) => { mgrSingleton.refreshSessionInfo() }
                );
        }
     );


    /**
     * Factory for PageView instances - maries YUI class inheritance with Typescript type system
     * @class PageViewFactory
     */
    export var Factory = {
        

        /**
         * Returns a Promise - the view loads various configuration 
         * information (templates, whatever) at the template, and 
         * fulfills the promise when the data is ready.
         * @method get
         * @for Module
         * @static
         * @return {Promise{PageView}}
         */
        get: function (): AuthManager {
            return mgrSingleton;
        }
    };

    //-----------------------------------

    /**
     * @method buildTestSuite
     */
    export function buildTestSuite(): Y.Test_TestSuite {
        var suite: Y.Test_TestSuite = new Y.Test.TestSuite("littleware-auth-authService");
        suite.add(new Y.Test.TestCase(
                {
                    // 
                    // just try to authenticate
                    //
                    testAuth: function () {
                        var mgr: AuthManager = Factory.get();

                        mgr.logout().then(
                            (sinfo: SessionInfo) => {
                                log.log("logout result: ");
                                console.dir(sinfo);
                                log.log("post logout session info: ");
                                console.dir(mgr.getSessionInfo());
                                Y.Assert.isTrue(!sinfo.getCreds(), "logout() works");
                                Y.Assert.isTrue(sinfo.getId() == mgr.getSessionInfo().getId(), "sessionInfo updated after logout");
                                return mgr.authenticate("littleware.test_user", "testSecret");
                            }
                        ).then(
                          (sinfo: SessionInfo) => {
                                log.log("auth result:");
                                console.dir(sinfo);
                                Y.Assert.isTrue(sinfo.getCreds() && (sinfo.getCreds().getUser() == "littleware.test_user"),
                                    "authentication looks ok"
                                    );
                                Y.Assert.isTrue(sinfo.getId() == mgr.getSessionInfo().getId(), "sessionInfo updated after authentication");
                              return mgr.refreshSessionInfo();
                            }
                        ).then( 
                          (sinfo: SessionInfo) => {
                              log.log("sessionInfo result:");
                              console.dir(sinfo);

                                this.resume(() => {
                                    Y.Assert.isTrue(sinfo.getCreds() && (sinfo.getCreds().getUser() == "littleware.test_user"),
                                        "auth chain looks ok"
                                        );
                                });
                            },
                            (err) => {
                                log.log("auth chain failed");
                                console.dir(err);
                                this.resume(() => {
                                    Y.Assert.fail("Auth chain failed");
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
