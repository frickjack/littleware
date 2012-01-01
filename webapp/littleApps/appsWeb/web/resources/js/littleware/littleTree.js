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
 * @module littleware.littleTree
 * @namespace littleware.littleTree
 */
YUI.add( 'littleware-littleTree', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleTree = (function() {
        var log = new Y.littleware.littleUtil.Logger( "littleTree" );
        
        /**
         * Little helper class implements simple template setup -
         * just provides an apply method that replaces
         * strings of form $name with a given string
         * 
         * @class LittleTemplate
         * @constructor
         * @param templateString
         */
        function LittleTemplate( templateString ) {
            this.templateString = templateString;
        }
        
        LittleTemplate.prototype.apply = function( varMap ) {
            var template = this.templateString
            for( var key in varMap ) {
                var value = varMap[key];
                var rx = new RegExp( "\\$" + key, "g" );
                template = template.replace( rx, value );
            }
            return template;
        }
        
        
        //------------------------------------------------
        
        /**
         * TreeNode - little helper class for building up a tree model
         * 
         * @class TreeNode
         * @constructor
         * @param name by which to build up the path to the node in the tree
         * @param labelHTML inner html to label node with
         * @param children array of child nodes - defaults to empty
         */
        function TreeNode( name, labelHTML, children ) {
            this.name = name;
            if ( ! Y.Lang.isArray( children ) ) {
                children = [];
            }
            if ( Y.Lang.isUndefined( labelHTML ) ) {
                labelHTML = name;
            }
            this.labelHTML = labelHTML;
            this.children = children;
        }
        
        //-----------------------------------------------
        
        /**
         * Controller manages a DOM-based tree widget in a given div
         * 
         * @class TreeController
         * @constructor
         * @param sDivSelector {String} in which to construct the tree
         */
        function TreeController( sDivSelector ) {
            this.sDivSelector = sDivSelector;
            this.treeModel = new TreeNode( "/", "<b>/</b>", 
                    [ new TreeNode( "A" ), new TreeNode( "B" ), new TreeNode( "C" ) ] 
                );
            
            this.selectedPaths = [];
            this.openPaths = [];
            this.state = "new";
            this.wrapper = null;
            this.nodeTemplate = Y.one("#treeNodeTemplate").get( 'text' );
        }
        
        TreeController.prototype.open = function(path) {
            
        };
        TreeController.prototype.close = function(path) {
            
        };
        TreeController.prototype.render = function() {
            if ( this.state != "new" ) {
                log.log( "Tree not in 'new' state ..." );
                return;
            }
            var div = Y.one( this.sDivSelector );
            var wrapper = Y.Node.create( "<div></div>" );
            wrapper.generateID();
            //wrapper.set( "id", this.sDivSelector + this.magic );
            this.state = "rendered";
            this.wrapper = wrapper;

            // setup root tree node
            var node = Y.Node.create( this.nodeTemplate );
            node.one( ".nodeLabel" ).setContent( this.treeModel.labelHTML );
            log.log( "Setting up root with label: " + this.treeModel.labelHTML );
            this.wrapper.appendChild( node );
            div.appendChild( this.wrapper );
        };
        
        TreeController.prototype.reRender = function() {
            
        };
        TreeController.prototype.setTreeModel = function( value ) {
          this.treeModel = value;  
        };
        
        //----------------------------------
        
        /**
         * Factory for new TreeControllers.  Should only attach a single
         * controller to any given div.  Exposed as a singleton at
         *   littleTree.TreeFactory
         *
         * @class TreeFactory
         * @constructor
         */
        function TreeFactory() {            
        };
        /**
         * Build a new TreeController
         * 
         * @param sDivSelector Y.one selector for div to attach controller to
         * @return {TreeController} new TreeController
         * @method build
         */
        TreeFactory.prototype.build = function( sDivSelector ) {
            return new TreeController( sDivSelector );
        };
        
        //----------------------------------
        
        /**
         * Return a test suite to test this submodule.
         * Test suite expects the hosting page to supply a
         * div with id #testSuiteTree to build a test tree in,
         * and a treeNodeTemplate script block of type text/x-template
         * or similar
         * 
         * @class LittleTree
         * @static
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-littleTree Test Suite");
            suite.add( new Y.Test.Case( {
                name: "LittleTree Test Case",
                testTemplates: function() {
                    var template = new Y.littleware.littleTree.LittleTemplate( "<b>Test!</b><br /><p>$testVar</p>" );
                    var result = template.apply( {testVar:"Super Bad!"});
                    Y.Assert.isTrue( result == "<b>Test!</b><br /><p>Super Bad!</p>", "Got expected template result: " + result  );
                },
                testSimpleTree: function() {
                    Y.one( "#testSuiteTree" ).setContent( "" );
                    var tree = Y.littleware.littleTree.TreeFactory.build( "#testSuiteTree" );
                    tree.render();
                }
            }
            ));
            return suite;
        };


        //---------------------------------------
        
        return {
          TreeFactory: new TreeFactory(),
          buildTestSuite: buildTestSuite,
          TreeNode: TreeNode,
          LittleTemplate: LittleTemplate
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'node', 'littleware-littleUtil']
});

