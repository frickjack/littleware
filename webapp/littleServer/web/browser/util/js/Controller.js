/*
 * Copyright 2010, catdogboy@yahoo.com
 * General use subject to GPLv2: http://www.gnu.org/licenses/gpl-2.0.html
 */



/**
 * Controller module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 *
 * @module Controller
 * @namespace littleware.browser
 */
YUI.add( "littleware.browser.Controller",  function(Y) {
    Y.namespace( "littleware.browser" )
    var littleware = Y.littleware

    littleware.browser.Controller = function() {

        /**
     * Controller class provides methods to handle UI events
     * @class Controller
     */
        var Controller = function() {}

    
        /**
     * Retrieve the data for the given asset id
     * @method loadAsset
     * @param path {string} assetPath
     * @return {littleware.browser.Model.BrowserModel}
     */
        Controller.prototype.loadAsset = function(path) {};


        //........................................

        /**
     * Basic implementation of Controller
     * @class AjaxController
     * @extends ControllerBuilder
     * @param builder {ControllerBuilder}
     */
        var AjaxController = function(builder) {
            this.browser = builder.browser;
            return this;
        };

        AjaxController.prototype.loadAsset = function(path) {

        };


        // ............................................
    
        /**
     * Controller provider.
     * @class ControllerBuilder
     */
        var ControllerBuilder = function() {
            /**
         * @property browser {Browser}
         */
            this.browser = null;
            return this;
        };

        /**
     * Set the id of the div in which to build the browser
     * @method setBrowser
     * @param value {Browser}
     * @return {ControllerBuilder} this
     */
        ControllerBuilder.prototype.setBrowser = function(value) {
            this.browser = value;
            return this;
        }
    
        /**
     * Controller provider function.
     * Controller is a singleton.
     * @method get
     * @return {Controller}
     */
        ControllerBuilder.prototype.get = function () {
            if ( null == this.browser ) {
                throw "ValidationException: Browser property not set";
            }
            return new AjaxController( this.browser );
        };
    

        /**
     * Aplication wire-up class - bootstraps the app and sets up event handlers,
     * and exposes a component API to external consumers.
     * @class Browser
     * @param divId {string}
     * @param controlBuilder {ControllerBuilder}
     */
        var Browser = function(divId,controlBuilder) {
            this.divId = divId;
            this.controller = controlBuilder.setBrowser( this ).get();
            this._init()
            return this;
        };

        /**
     * Initialize the browser
     * @method init
     * @private
     */
        Browser.prototype._init = function() {

        };

        /**
     * Load the specified path into the browser
     * @method loadPath
     * @param path {string}
     */
        Browser.prototype.loadPath = function(path) {}

        /**
     * Get the path active in the browser
     * @return {string} active path
     */
        Browser.prototype.getPath = function() {
            return "";
        }

        // ................................................

        /**
     * Singleton browser app
     * @class BrowserBuilder
     * @param divId {string} id of div in which to manage a browser
     */
        var BrowserBuilder = function(divId) {
            this.divId = divId;
            this.assetPath = "/";
            Y.log( "Hello from littleware!", "info", "littleware.browser.Controller" );
            return this;
        };

        /**
     * Setter for assetPath property - determines the initial
     * assetPath to begin browsing at
     */
        BrowserBuilder.prototype.setAssetPath = function (value) {
            this.assetPath = value;
            return this;
        }

        /**
     * Builder method - Browser is a singleton
     * @method get
     * @return {Browser}
     */
        BrowserBuilder.prototype.build = function() {
            var browser = new Browser(yui,divId, new ControllerBuilder() );
            return browser;
        };

        //  Export public methods to module
        return {
            BrowserBuilder: BrowserBuilder
        };
    }()
}, "0.0" );
