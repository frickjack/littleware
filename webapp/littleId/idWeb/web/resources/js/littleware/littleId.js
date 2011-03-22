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
 * @module littleware
 * @submodule littleId
 * @namespace littleware.littleId
 */
YUI.add('littleware-littleId', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleId = (function() {
        /**
         * Class manages an OpenID Login interaction
         *
         * @param config currently unused
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
        LoginProcess.openIdURL = "http://localhost:8080/openId/"
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
            }
        }

        Y.extend(LoginProcess, Y.Base, {
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
                        width: 250,
                        height: 200
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
                // Expose this object in the global namespace,
                // so the popup callback can access it
                littleware = Y.littleware;
                var ui = Y.one( this.get( "uiDivSelector" ) + " div.littleId_popupBody" )
                ui.get( 'children' ).remove();
                ui.append( "<p>Contacting " + this.get( "oidProvider" ) + " in popup ... <img src='" + 
                    LoginProcess.openIdURL + "resources/img/wait.gif' alt='waiting' /></p>" 
                    );
                //ui.append( "<iframe src='" + LoginProcess.openIdURL + "openId/services/authRequest/?provider=" + this.get( "oidProvider" ) + "'></iframe>" )
                window.open(LoginProcess.openIdURL + "openId/services/authRequest/?provider=" + this.get( "oidProvider" ),
                            'openid_popup', 'width=790,height=580'
                        );
                this._set( 'loginState', 'Waiting4Provider' )
            },
            handleProviderCallback:function( callbackData ) {
                var ui = Y.one( this.get( "uiDivSelector" ) + " div.littleId_popupBody" )
                ui.get( 'children' ).remove();
                if( callbackData.authSuccess ) {
                    this.set( 'userCreds', callbackData.userCreds )
                    this.set( 'loginState', 'CredsReady')
                    ui.append( "<p>Credentials available for openId " + callbackData.userCreds.openId +
                             ", email " + callbackData.userCreds.email + "</p>"
                         )
                    var head = Y.one( this.get( "uiDivSelector" ) + " div.littleId_popupHead" )
                    head.get( 'children' ).remove
                    var closeLink = Y.Node.create( "<a href='.'>X Close</a>" )
                    head.appendChild( closeLink )
                    Y.on( 'click', function(ev) {
                        ev.preventDefault();
                        Y.one( this.get( "uiDivSelector" ) ).setStyle( 'display', 'none' );
                    }, closeLink );
                } else {
                    this.set( 'loginState', 'FailedAuth')
                    ui.append( "<p>Authentication Failed</p>" );
                }
            }
        });

        function CallbackData( authSuccess, authId, authEmail ) {
            if ( Y.Lang.isUndefined( authSuccess )
                || Y.Lang.isUndefined( authId )
                || Y.Lang.isUndefined( authEmail )
                ) {
                this.authSuccess = false;
            } else {
                this.authSuccess = authSuccess;
            }
            if ( this.authSuccess ) {
                this.userCreds = {
                    openId:authId,
                    email:authEmail
                };
            }
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
                    login.promptUserForProvider( "#promptTest" )
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
    requires: ['base', 'anim', 'node-base']
});
