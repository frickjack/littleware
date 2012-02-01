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
         * @param templateString is the template contents
         */
        function LittleTemplate( templateString ) {
            this.templateString = templateString;
        }
        
        LittleTemplate.prototype.applyFilter = function( varMap ) {
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
         * @extends Base
         */
        function TreeController( sDivSelector ) {
            // Invoke Base constructor, passing through arguments
            TreeController.superclass.constructor.apply(this );

            this.sDivSelector = sDivSelector;
            this.selectedPaths = [];
            this.openPaths = [];
            this.state = "new";
            this.wrapper = null;
            this.nodeTemplate = Y.one("#treeNodeTemplate").get( 'text' );
        }
        
        /**
         * YUI.Base property mechanism with setter psuedo-event
         */
        TreeController.ATTRS = {
            treeModel: {
                value:new TreeNode( "/", "<b>/</b>", //[] ),
                    [ new TreeNode( "A", "<b>A</b>", [ new TreeNode( "AAA") ] ), 
                    new TreeNode( "B" ), new TreeNode( "C" ) 
                    ]
                    ),
                readOnly:false        
            }
        };

        /*
            treeModel = new TreeNode( "/", "<b>/</b>", 
                [ new TreeNode( "A", "<b>A</b>", [ new TreeNode( "AAA") ] ), 
                    new TreeNode( "B" ), new TreeNode( "C" ) 
                ] 
                );

 *            */

        var TreeControllerInternal = function( tree ) {
            /**
             * Internal render helper - builds and appends child
             * nodes to the given parent node - recursively depth first
             * 
             * @method renderChildren
             * @private
             * @param tree {TreeController} to render
             * @param parent {TreeNode} to scan for children
             * @param parentNode {YUI.Node} UL or OL node to append children to
             */
            function renderChildren( parent, parentNode ) {
                var children = parent.children;
                var ul = parentNode.one( "ul.nodeChildren" );
                for ( var i=0; i < children.length; ++i ) {
                    var li = Y.Node.create( "<li></li>" );
                    var kid = children[i];
                    var kidNode = uiNodeFactory( kid );
                    li.appendChild( kidNode );
                    ul.appendChild( li );
                    renderChildren( kid, kidNode );
                }
                parentNode.one( ".treeToggle" ).on( "click",
                    openCloseListener, tree
                    );
            };
            
            
            /**
             * Internal utility to toggle the display state of the given
             * littleTree subtree list
             * 
             * @method openCloseSubtree
             * @private
             * @param parentNode YUI node with ul.nodeChildren and -.treeToggle children
             */
            function openCloseSubtree( parentNode ) {
                var link = parentNode.one( ".treeToggle" );
                var ul = parentNode.one( "ul.nodeChildren" );
                ul.toggleClass( "littleTreeClosed" );
                if ( ul.hasClass( "littleTreeClosed" )) {
                    link.setContent( "[+]&nbsp;");
                } else {
                    link.setContent( "[-]&nbsp;");
                }
            };
            
            /**
             * Internal listener for subtree open-close onClick event
             * 
             * @method openCloseListener
             * @private
             * @param event
             */
            function openCloseListener( event ) {
                event.preventDefault();
                var link = event.target;
                var parent = link.ancestor( ".treeNode" );
                openCloseSubtree( parent );
            };
            
            
        
            /**
             * Internal factory allocates a YUI.Node conforming to the tree's template,
             * and with label from the treeNode
             * 
             * @method uiNodeFactory
             * @private
             * @param tree TreeController to extract template from
             * @param treeNode to extract label from
             * @return Yui.Node
             */
            function uiNodeFactory( treeNode ) {
                var node = Y.Node.create( tree.nodeTemplate );
                node.one( ".nodeLabel" ).setContent( treeNode.labelHTML );
                return node;
            };
            
            return {
                uiNodeFactory: uiNodeFactory,
                renderChildren: renderChildren
            }
        };

        var TCMethods = {
            open : function(path) {
            
            },
            close : function(path) {
            
            },
        
        
            /**
             * Render the current tree model element selected by sDivSelector
             * supplied to constructor.  Typical control flow: 
             *  <pre>
             *     var tree = Y.littleware.littleTree.TreeFactory.build( "#myTreeDiv" );
             *     tree.setModel( ... );
             *     tree.render();
             *  </pre>
             * 
             * @method render
             */
            render : function() {
                if ( this.state != "new" ) {
                    log.log( "Tree not in 'new' state ..." );
                    return;
                }
                var div = Y.one( this.sDivSelector );
                var wrapper = Y.Node.create( "<div></div>" );
                wrapper.generateID();
                //wrapper.set( "id", this.sDivSelector + this.magic );
                this.wrapper = wrapper;
                div.appendChild( this.wrapper );
                this.state = "rendered";                
                this.reRender();
                this.after( "treeModelChange", function(event) {
                    log.log( "Processing treeModelChange event ..." );
                    this.reRender();
                } );
            },
        
            reRender : function() {
                Y.Assert.isFalse( this.state == "new",
                    "reRender should not be called while in 'new' state ..." 
                    );
                var internal = TreeControllerInternal( this );
                var model = this.get( "treeModel" );
                var node = internal.uiNodeFactory( model );
                internal.renderChildren( model, node );
                // setup root tree node
                log.log( "Setting up root with label: " + model.labelHTML );
                this.wrapper.setContent( node );
            },
            
            /**
             * Shortcut for set( "treeModel", value )
             * 
             * @method setTreeModel
             */
            setTreeModel : function( value ) {
                this.set( "treeModel", value );  
            }
        };
        
        // YUI inheritance mechanism
        Y.extend(TreeController, Y.Base, TCMethods );
        
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
                    var result = template.applyFilter( {
                        testVar:"Super Bad!"
                    });
                    Y.Assert.isTrue( result == "<b>Test!</b><br /><p>Super Bad!</p>", "Got expected template result: " + result  );
                },
                testSimpleTree: function() {
                    var treeDiv = Y.one( "#testSuiteTree" );
                    treeDiv.setContent( "" );
                    var tree = Y.littleware.littleTree.TreeFactory.build( "#testSuiteTree" );
                    tree.render();
                    var testButton = Y.Node.create( "<div><a class=\"treeToggle\" href=\"#\">click</a> to toggle tree model</div>" );
                    var toggleModel = new TreeNode( "/", "<b>/</b>", //[] ),
                        [ new TreeNode( "D" ), new TreeNode( "E", "<b>E</b>", [ new TreeNode( "EEE") ] ), 
                             new TreeNode( "F" ) 
                        ]
                        );
                    testButton.delegate( 'click', 
                        function(ev) { 
                            ev.preventDefault();
                            tree.set( "treeModel", toggleModel ); 
                            testButton.hide();
                        },
                        "a.treeToggle" 
                        );
                    treeDiv.appendChild( testButton );
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
    requires: [ 'base', 'node', 'littleware-littleUtil']
});

