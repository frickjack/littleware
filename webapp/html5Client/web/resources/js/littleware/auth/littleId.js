/*
 * Copyright 2011 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * littleware.auth.littleId module,
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.auth.littleId
 * @namespace littleware.auth.littleId
 */
YUI.add('littleware-littleId', function(Y) {
    Y.namespace('littleware.auth');
    Y.littleware.auth.littleId = (function() {
        function log( msg, level ) {
            if( Y.Lang.isUndefined( level ) ) {
                level = 'info';
            }
            Y.log( msg, level, 'littleware-littleId' )
        }

        /**
         * Class manages an OpenID Login interaction
         *
         * @param config with attributes replyToURL and replyToMethod
         * @param formTemplate
         * @class LoginHelper
         * @constructor
         * @extends Base
         */
        function LoginHelper(config,formTemplate) {
            // Invoke Base constructor, passing through arguments
            LoginHelper.superclass.constructor.apply(this, config);
            this.formTemplate = formTemplate;
        }

        LoginHelper.NAME = "LoginHelper"
        LoginHelper.openIdURL = "http://localhost:8080/littleware_services/"
        LoginHelper.STATES = [ "NotYetStarted", "Started", "CanceledByUser", "Waiting4Provider", "CredsReady", "FailedAuth"];

        LoginHelper.ATTRS = {
            loginState: {
                value:"NotYetStarted",
                readOnly:true
            },
            userCreds: {
                value:undefined,
                readOnly:true
            },
            replyToURL: {
                value:undefined
            },
            replyMethod: {
                value:'POST'
            }
        }

        var util = Y.littleware.littleUtil; // shortcut


        // Define methods here, so NetBeans IDE navigator picks them up
        var LoginHelperMethods = {
            /**
             * Retrieve the endpoint information to post to an openId provider
             * to carry out openId authentication via the littleId service.
             *
             * @method prepareAuthRequest
             * @param provider currently littleId support yahoo and google
             * @return {Y.Promise{RequestInfo}} promise that resolves with openId auth request
             */
            prepareAuthRequest:function( oidProvider ){
                var openIdURL = LoginHelper.openIdURL + "openId/services/authRequest/?provider=" + oidProvider +
                "&replyMethod=" + this.get( 'replyMethod' );
                var replyTo = window.location.href;
                if ( Y.Lang.isValue( replyTo ) ) {
                    openIdURL = openIdURL + "&replyTo=" + escape( replyTo )
                }

                // load the form parameters from the littleId service
                return new Y.Promise((function (resolve, reject) {
                    Y.io(openIdURL, {
                        method: "GET",
                        on: {
                            complete: function (id, ev) {
                                log("login response");
                                console.dir(ev);
                                if (ev.status == 200) {
                                    var openIdInfo = JSON.parse(ev.responseText);
                                    log("openIdInfo: ");
                                    console.dir(openIdInfo);

                                    resolve(openIdInfo);
                                } else {
                                    reject(ev);
                                }
                            }
                        }
                    });
                }).bind(this));

                // populate and submit the form to the openId provider
                //this._set( 'loginState', 'Waiting4Provider' )
            },

            /**
             * Construct a HTML form ready to submit to an openId provider
             * 
             * @method postToProvider
             * @param openIdRequest {Object} request from prepareAuthRequest
             * @return {Node} form node ready to append to DOM and submit
             */
            buildProviderForm: function (openIdRequest) {
                var content = this.formTemplate(openIdRequest);
                log("Building form with content: " + content);
                return Y.Node.create(content);
            },

            handleProviderCallback:function( callbackData ) {
                this._set('loginState', 'handlingCallback');
                if( callbackData.authSuccess ) {
                    this._set( 'userCreds', callbackData.userCreds )
                    this._set( 'loginState', 'CredsReady')
                    log("<p>Credentials available: <ul><li>openId: " + callbackData.userCreds.openId +
                        "</li><li>email: " + callbackData.userCreds.email + "</li></ul></p>"
                        );
                } else {
                    this._set( 'loginState', 'FailedAuth')
                    log("<p>Authentication Failed</p>");
                    console.dir(callbackData);
                }
            }
        };

        Y.extend(LoginHelper, Y.Base, LoginHelperMethods);

        //--------------------- CallbackData data object -------------------

        /**
         * Pojo holds open-id callback data
         *
         * @param authSuccess true if authorization succeeded, else false
         * @param authId openId string
         * @param autEmail email string
         * @param verifySecret string to verify id/email with
         * @class CallbackData
         * @constructor
         */
        function CallbackData( authSuccess, authId, authEmail, verifySecret ) {
            if ( Y.Lang.isUndefined( authSuccess )
                || Y.Lang.isUndefined( authId )
                || Y.Lang.isUndefined( authEmail )
                || Y.Lang.isUndefined( verifySecret )
                ) {
                this.authSuccess = false;
            } else {
                this.authSuccess = authSuccess;
            }
            if ( this.authSuccess ) {
                this.userCreds = {
                    openId:authId,
                    email:authEmail,
                    secret:verifySecret
                };
            }
        }

        /**
         * Static method runs a simple test on the given url to determine whether
         * the url has query parameters suitable for CallbackData.buildFromURL
         *
         * @method isCallbackURL
         * @static
         * @param {string} url possibly with query parameters
         * @return {boolean}
         */ 
        CallbackData.isCallbackURL = function(url) {
            return url.match( /\?.*authSuccess/ );
        }

        /**
         * Static method to build callback data from GET parameters attached to a window -
         *    Y.littleware.auth.littleId.CallbackData.buildFromURL( window.location.href )
         *
         * @method buildFromURL
         * @static
         * @param {string} url possibly with query parameters
         * @return {CallbackData} data extracted from given url's query parameters otherwise
         *    populated with defaults: 
         *          { auth: false, email: "unknown@unknown", secret: "unknown", openId:"https://unknown" }
         */
        CallbackData.buildFromURL = function(url) {
            var authMatch = /authSuccess=(\w+)/.exec(url);
            var emailMatch = /email=([^&]+)/.exec(url);
            var idMatch = /openId=([^&]+)/.exec(url);
            var secretMatch = /verifySecret=([^&]+)/.exec(url);

            var auth = false;
            var email = "unknown@unknown";
            var openId = "https://unknown";
            var secret = "unknown";

            if ( authMatch ) {
                auth = (authMatch[1] == 'true');
            } else {
                log("Failed to extract auth from url " + url);
            }
            if ( emailMatch ) {
                email = unescape(emailMatch[1]);
            } else {
                log("Failed to extract email from url " + url);
            }
            if ( idMatch ) {
                openId = unescape(idMatch[1]);
            } else {
                log("Failed to extract openId from url " + url);
            }
            if ( secretMatch ) {
                secret = unescape(secretMatch[1]);
            } else {
                log("Failed to extract secret from url " + href);
            }
            return new CallbackData(auth, openId, email, secret);
        }

        //------- TestSuite stuff ------------------------------------------

        /**
         * Return a test suite to test this submodule
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-littleId Test Suite");
            suite.add( new Y.Test.Case( {
                name: "LoginHelper Test Case",
                testLPEvents: function() {
                    Y.littleware.auth.littleId.helperFactory.get().then(
                        function (helper) {
                            this.resume(function () {
                                var callback = false;
                                helper.after('loginStateChange', function (ev) {
                                    callback = true;
                                })
                                Y.Assert.isTrue(helper.get('loginState') == "NotYetStarted",
                                    "LoginHelper initial state is as expected: " + helper.get('loginState')
                                    );
                                helper._set('loginState', "CanceledByUser");  // should trigger changed-attribute event
                                Y.Assert.isTrue(helper.get('loginState') == "CanceledByUser", "Post-set loginState ok: " + helper.get('loginState'));
                                Y.Assert.isTrue(callback, "attribute-change envent handled as expected");
                            });
                        }.bind(this),
                        function (err) {
                            this.resume(function () {
                                log.log("testLPEvents failed to load helper");
                                console.dir(err);
                                Y.Assert.fail("Failed to load LoginHeler");
                            });
                        }.bind(this) );
                    this.wait(2000);
                },
                testCallbackData:function() {
                    var dataArray = {
                        basic: new CallbackData( true, 'http://id', 'email@email', 'secret' ),
                        href: CallbackData.buildFromURL( 'http://bla?authSuccess=true&openId=http://id&email=email@email&verifySecret=secret' )
                    };
                    for( index in dataArray
                        ) {
                        var data = dataArray[index];
                        Y.Assert.isTrue( data.authSuccess, index + ' authSuccess property set ok')
                        Y.Assert.isTrue( data.userCreds.openId == 'http://id', index + ' Got expected openId: ' + data.userCreds.openId )
                        Y.Assert.isTrue( data.userCreds.email == 'email@email', index + ' Got expected email: ' + data.userCreds.email )
                        Y.Assert.isTrue( data.userCreds.secret == 'secret', index + ' Got expected secret: ' + data.userCreds.secret )
                    }
                }
            }
            ));
            return suite;
        };

        //------------ module export stuff ----------------------------

        // template for provider-submission form
        var templatePath = "/littleware_apps/resources/templates/littleware/auth/providerForm.handlebars";
        log("Loading template: " + templatePath);
        var templatePromise = util.loadHandlebar(templatePath
            ).then(
                function (tplate) {
                    log("Loaded template ...");
                    return tplate;
                },
                function (err) {
                    log("Failed to load template: " + err);
                }
            );


        var helperSingleton = templatePromise.then( function(tpl) { return new LoginHelper( {}, tpl ); } );

        // expose an api
        return {
            helperFactory: {
                get: function() { return helperSingleton; }
            },
            CallbackData:CallbackData,
            buildTestSuite: buildTestSuite
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'anim', 'base', 'node-base']
});
