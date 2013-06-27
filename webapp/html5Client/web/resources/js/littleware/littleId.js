/*
 * Copyright 2011 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * littleware.littleId module,
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.littleId
 * @namespace littleware.littleId
 */
YUI.add('littleware-littleId', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleId = (function() {
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
         *
         * @class LoginProcess
         * @constructor
         * @extends Base
         */
        function LoginProcess(config) {
            // Invoke Base constructor, passing through arguments
            LoginProcess.superclass.constructor.apply(this, config);
        }

        LoginProcess.NAME = "LoginProcess"
        LoginProcess.openIdURL = "http://beta.frickjack.com:8080/services/"
        LoginProcess.STATES = [ "NotYetStarted", "Started", "CanceledByUser", "Waiting4Provider", "CredsReady", "FailedAuth"];

        LoginProcess.ATTRS = {
            loginState: {
                value:"NotYetStarted",
                readOnly:true
            },
            userCreds: {
                value:undefined,
                readOnly:true
            },
            oidProvider: {
                value:'google'
            },
            uiDivSelector: {
                value:'#littleIdUI',
                readOnly:true
            },
            replyToURL: {
                value:undefined
            },
            replyMethod: {
                value:'POST'
            }
        }

        // Define methods here, so NetBeans IDE navigator picks them up
        var LoginProcessMethods = {
            // Prototype methods for your new class
            /**
             * Move into the 'Started' state, and prompt the user to select a provider
             *
             * @method promptUserForProvider
             */
            promptUserForProvider:function(){
                var selector = this.get( 'uiDivSelector')
                var node = Y.one( selector )
                if ( Y.Lang.isNull( node ) ) {
                    throw new Error( "No such node: " + divId )
                }
                {
                    var state = this.get( 'loginState' )

                    if( (state == 'Started') || (state == 'Waiting4Provider') ) {
                        throw new Error( "LoginProcess in invalid state for action: " + state)
                    }
                }
                node.setStyle( "height", "0px" );
                node.setStyle( "width", "0px" );
                node.setStyle( "display", "block" );
                node.get( 'children' ).remove();

                var loginProcess = this
                var closeLink = Y.Node.create( "<a href=''>X Cancel Login</a>" );
                Y.on( 'click', function(ev) {
                    ev.preventDefault();
                    node.setStyle( 'display', 'none' );
                    loginProcess._set( 'loginState', 'CanceledByUser');
                }, closeLink
                );

                var yahooLink = Y.Node.create( "<a href='#'>Sign in with <br /> <img src='" + LoginProcess.openIdURL + "resources/img/yahoo.png' /></a>")
                Y.on( 'click', function(ev) {
                    ev.preventDefault();
                    loginProcess.set( 'oidProvider', 'yahoo' )
                    loginProcess.authenticateWithProvider()
                },
                yahooLink
                )

                var googleLink = Y.Node.create( "<a href='#'>Sign in with <br /> <img src='" + LoginProcess.openIdURL + "resources/img/google.png' /></a>" )
                Y.on( 'click', function(ev) {
                    ev.preventDefault();
                    loginProcess.set( 'oidProvider', 'google' )
                    loginProcess.authenticateWithProvider()
                },
                googleLink
                )

                var headNode = Y.Node.create( "<div class='littleId_popupHead'></div>" );
                headNode.appendChild( closeLink );
                var bodyNode = Y.Node.create( "<div class='littleId_popupBody'></div>" );
                var providers = Array(googleLink,yahooLink)
                for( var index in providers ) {
                    var p = Y.Node.create( "<p></p>" )
                    p.appendChild( providers[index] )
                    bodyNode.appendChild( p )
                }
                var footNode = Y.Node.create( "<div class='littleId_popupFoot'><a href='#'>frickjack.com/openId services</a></div>" );
                var myAnim = new Y.Anim({
                    node: selector,
                    to: {
                        width: 350,
                        height: 240
                    },
                    duration: 0.5
                });
                this._set( 'loginState', 'Started' )
                this._set( 'userCreds', undefined );
                myAnim.on( 'end', function() {
                    node.appendChild( headNode )
                    node.appendChild( bodyNode );
                    node.appendChild( footNode )
                }
                );
                myAnim.run();
            },
            /**
             * Launch a popup window in which the user will authenticate
             * with the OID provider.  Exposes this object in the global
             * namespace at littleware.littleId.LoginProcess, so the
             * popup-window window.parent callback can access it.
             */
            authenticateWithProvider:function(){
                var ui = Y.one( this.get( "uiDivSelector" ) + " div.littleId_popupBody" )
                ui.get( 'children' ).remove();
                ui.append( "<p>Contacting " + this.get( "oidProvider" ) + " in popup ... <img src='" +
                    LoginProcess.openIdURL + "resources/img/wait.gif' alt='waiting' /></p>"
                    );
                //ui.append( "<iframe src='" + LoginProcess.openIdURL + "openId/services/authRequest/?provider=" + this.get( "oidProvider" ) + "'></iframe>" )
                var openIdURL = LoginProcess.openIdURL + "openId/services/authRequest/?provider=" + this.get( "oidProvider" ) +
                "&replyMethod=" + this.get( 'replyMethod' );
                var replyTo = this.get( 'replyToURL' );
                if ( Y.Lang.isValue( replyTo ) ) {
                    openIdURL = openIdURL + "&replyTo=" + escape( replyTo )
                }
                window.open( openIdURL,
                    'openid_popup', 'width=790,height=580'
                    );
                this._set( 'loginState', 'Waiting4Provider' )
            },
            handleProviderCallback:function( callbackData ) {
                if ( ! this.get( 'loginState' ) == 'Waiting4Provider' ) {
                    log( "Ignoring provider callback, not in waiting state: " + this.get( 'loginState'));
                }
                var ui = Y.one( this.get( "uiDivSelector" ) + " div.littleId_popupBody" )
                ui.get( 'children' ).remove();
                if( callbackData.authSuccess ) {
                    this._set( 'userCreds', callbackData.userCreds )
                    this._set( 'loginState', 'CredsReady')
                    ui.append( "<p>Credentials available: <ul><li>openId: " + callbackData.userCreds.openId +
                        "</li><li>email: " + callbackData.userCreds.email + "</li></ul></p>"
                        )
                    var head = Y.one( this.get( "uiDivSelector" ) + " div.littleId_popupHead" )
                    head.get( 'children' ).remove();
                    var closeLink = Y.Node.create( "<a href='.'>X Close</a>" )
                    var node = Y.one( this.get( "uiDivSelector" ) )
                    head.appendChild( closeLink )
                    Y.on( 'click', function(ev) {
                        ev.preventDefault();
                        node.setStyle( 'display', 'none' );
                    }, closeLink );
                } else {
                    this._set( 'loginState', 'FailedAuth')
                    ui.append( "<p>Authentication Failed</p>" );
                }
            }
        };

        Y.extend(LoginProcess, Y.Base, LoginProcessMethods );

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
         * Static method to build callback data from GET parameters attached to a window -
         *    Y.littleware.littleId.CallbackData.buildFromHref( window.location.href )
         */
        CallbackData.buildFromHref = function(href) {
            var authMatch = /authSuccess=(\w+)/.exec( href )
            var emailMatch = /email=([^&]+)/.exec( href )
            var idMatch = /openId=([^&]+)/.exec( href )
            var secretMatch = /verifySecret=([^&]+)/.exec( href )

            var auth = false
            var email = "unknown@unknown"
            var openId = "https://frickjack"
            var secret = "unknown"

            if ( authMatch ) {
                auth = (authMatch[1] == 'true');
            } else {
                log( "Failed to extract auth from href " + href )
            }
            if ( emailMatch ) {
                email = unescape( emailMatch[1] )
            } else {
                log( "Failed to extract email from href " + href )
            }
            if ( idMatch ) {
                openId = unescape( idMatch[1] )
            } else {
                log( "Failed to extract openId from href " + href )
            }
            if ( secretMatch ) {
                secret = unescape( secretMatch[1] )
            } else {
                log( "Failed to extract secret from href " + href )
            }
            return new CallbackData( auth, openId, email, secret )
        }

        /**
         * Return a test suite to test this submodule
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-littleId Test Suite");
            suite.add( new Y.Test.Case( {
                name: "LoginProcess Test Case",
                testLPEvents: function() {
                    var login = Y.littleware.littleId.LoginProcess
                    var callback = false;
                    login.after( 'loginStateChange', function(ev) {
                        callback = true;
                    })
                    Y.Assert.isTrue( login.get( 'loginState' ) == "NotYetStarted",
                        "LoginProcess initial state is as expected: " + login.get( 'loginState' )
                        );
                    login._set( 'loginState',  "CanceledByUser" );  // should trigger changed-attribute event
                    Y.Assert.isTrue( login.get( 'loginState' ) == "CanceledByUser", "Post-set loginState ok: " + login.get( 'loginState' ) );
                    Y.Assert.isTrue( callback, "attribute-change envent handled as expected" );
                    Y.Assert.isNull( Y.one( "#bogusNode" ), "Y.one(#bogusNode) is null")
                },
                testLoginPrompt: function() {
                    var login = Y.littleware.littleId.LoginProcess
                    login.promptUserForProvider()
                },
                testCallbackData:function() {
                    var dataArray = {
                        basic: new Y.littleware.littleId.CalbackData( true, 'http://id', 'email@email', 'secret' ),
                        href: Y.littleware.littleId.CalbackData.buildFromHref( 'http://bla?authSuccess=true&openId=http://id&email=email@email&verifySecret=secret' )
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
        // expose an api
        return {
            LoginProcess:new LoginProcess(),
            CalbackData:CallbackData,
            buildTestSuite: buildTestSuite
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'anim', 'base', 'node-base']
});
