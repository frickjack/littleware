/*
 * Copyright 2010, catdogboy@yahoo.com
 * General use subject to GPLv2: http://www.gnu.org/licenses/gpl-2.0.html
 */

YUI.namespace( "littleware.browser" )
littleware = YUI.littleware


/**
 * Controller module, see http://yuiblog.com/blog/2007/06/12/module-pattern/
 *
 * @module Controller
 * @namespace littleware.browser
 */
littleware.browser.Controller = function() {

    /**
     * Controller class provides methods to handle UI events
     * @class Controller
     * @param browser {Browser}
     */    
    var Controller = function(browser) {
        this.browser = browser;
        return this;
    };

    /**
     * Retrieve the data for the given asset id
     * @method loadAsset
     * @param path {string} assetPath
     * @return {littleware.browser.Model.BrowserModel}
     */
    Controller.prototype.loadAsset = function(path) {

    };


    
    /**
     * Singleton controller provider - in file scope.
     * @class ControllerBuilder
     */
    var ControllerBuilder = function() {
        this.browser = null;
        return this;
    };

    /**
     * Setter for browser property
     */
    ControllerBuilder.prototype.setBrowser = function(value) {
        this.browser = value;
        return this;
    }
    
    /**
     * Controller provider function.
     * Controller is a singleton.
     * @method getController
     * @return {Controller}
     */
    ControllerBuilder.prototype.get = function () {
        if ( null == this.browser ) {
            throw "ValidationException: Browser property not set";
        }
        return new Controller( this.browser );
    };
    

    /**
     * Aplication wire-up class - bootstraps the app and sets up event handlers
     * @class Browser
     * @param yui {YUI}
     * @param divId {string}
     * @param controlBuilder {ControllerBuilder}
     */
    var Browser = function(yui,divId,controlBuilder) {
        this.yui = yui;
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
     * Singleton browser app
     * @class BrowserBuilder
     * @param yui {YUI} yui 3 handle
     * @param divId {string} id of div in which to manage a browser
     */
    var BrowserBuilder = function(yui,divId) {
        this.yui = yui;
        this.divId = divId;
        this.assetPath = "/";
        yui.log( "Hello from littleware!", "info", "littleware.browser.Controller" );
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
    };

    //  Export public methods to module 
    return {
        BrowserBuilder: BrowserBuilder
    };
} ();
