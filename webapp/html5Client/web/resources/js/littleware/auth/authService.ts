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
         * Get the currently active session.
         * May return cached credentials if optForceCheck is not set true.
         * @method getCreds
         * @param optForceCheck {boolean}
         * @return {Promise{SessionInfo}} promise delivers session info
         */
        getSessionInfo(optForceCheck?: boolean): Y.Promise<SessionInfo>;

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
    var authServiceRoot = "http://localhost:8080/littleware_services/";

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


    class SimpleAuthManager {

        private sessionInfoPromise: Y.Promise<SessionInfo> = null;

        getSessionInfo(optForceCheck?: boolean): Y.Promise<SessionInfo> {
            if ( this.sessionInfoPromise) {
                return this.sessionInfoPromise;
            }

            this.sessionInfoPromise = new Y.Promise(
                (resolve, reject) => {
                    Y.io(authServiceRoot + "/auth/login", {
                        method: "GET",
                        on: {
                            complete: function (id, ev) {
                                log.log("sessionInfo response");
                                console.dir(ev);
                                this.sessionInfoPromise = null;

                                if (ev.status == 200) {
                                    var json = JSON.parse(ev.responseText);
                                    var creds: Creds = null;

                                    if (json.authToken) {
                                        creds = new Creds( json.user || "user", json.authToken, new Date( json.authExpires ) );
                                    }

                                    var session: SessionInfo = new SessionInfo(json.id, creds);
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
        }


        private loginPromise: Y.Promise<SessionInfo> = null;

        authenticate(user: string, secret: string): Y.Promise<SessionInfo> {
            if (this.loginPromise) {
                return this.loginPromise;
            }
            var url = authServiceRoot + "/auth/login?action=login&user=" +
                encodeURIComponent(user) +
                "&password=" + encodeURIComponent(secret);
            this.loginPromise = new Y.Promise(
                (resolve, reject) => {
                    Y.io( url, {
                        method: "GET",
                        on: {
                            complete: function (id, ev) {
                                log.log("login response");
                                console.dir(ev);
                                if (ev.status == 200) {
                                    var json = JSON.parse(ev.responseText);
                                    var creds: Creds = null;
                                    if (json.authToken) {
                                        creds = new Creds( json.user || "user", json.authToken, new Date(json.authExpires));
                                    }

                                    var session: SessionInfo = new SessionInfo(json.id, creds);
                                    log.log("login session: ");
                                    console.dir(session);

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
        }



        private logoutPromise: Y.Promise<SessionInfo> = null;

        logout(): Y.Promise<SessionInfo> {
            if (this.logoutPromise) {
                return this.logoutPromise;
            }

            this.logoutPromise = new Y.Promise(
                (resolve, reject) => {
                    Y.io(authServiceRoot + "/auth/login?action=logout", {
                        method: "GET",
                        on: {
                            complete: function (id, ev) {
                                log.log("logout response");
                                console.dir(ev);
                                if (ev.status == 200) {
                                    var json = JSON.parse(ev.responseText);
                                    var creds: Creds = null;

                                    var session: SessionInfo = new SessionInfo(json.id, creds);
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
        }

    }


    var mgrSingleton: AuthManager = new SimpleAuthManager();

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
                                Y.Assert.isTrue(!sinfo.getCreds(), "logout() works");
                                return mgr.authenticate("testUser", "testSecret");
                            }
                        ).then(
                          (sinfo: SessionInfo) => {
                                log.log("auth result:");
                                console.dir(sinfo);
                                Y.Assert.isTrue(sinfo.getCreds() && (sinfo.getCreds().getUser() == "testUser"),
                                    "authentication looks ok"
                                    );
                                return mgr.getSessionInfo();
                            }
                        ).then( 
                          (sinfo: SessionInfo) => {
                              log.log("sessionInfo result:");
                              console.dir(sinfo);

                                this.resume(() => {
                                    Y.Assert.isTrue(sinfo.getCreds() && (sinfo.getCreds().getUser() == "testUser"),
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
