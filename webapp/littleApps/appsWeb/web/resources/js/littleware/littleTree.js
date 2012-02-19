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
 * TODO: add/remove children, dynamic load, node selection
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
        
        var nodeIdCounter = 0;
        
        /**
         * TreeNode - little helper class for building up a tree model
         * 
         * @class TreeNode
         * @constructor
         * @param name by which to build up the path to the node in the tree
         * @param labelHTML inner html to label node with
         * @param children array of child nodes - defaults to empty
         * @param config app-specific info to attach to object
         * @param listener function( event, TreeNode ) { ... } called on open/close subtree event
         *                   on this node
         */
        function TreeNode( name, labelHTML, children, config, listener ) {
            if ( ! Y.Lang.isArray( children ) ) {
                children = [];
            }
            if ( Y.Lang.isUndefined( labelHTML ) ) {
                labelHTML = name;
            }
            TreeNode.superclass.constructor.apply( this, [ { children: children } ] );
            this.name = name;
            this.labelHTML = labelHTML;
            this.appConfig = config;
            this.listener = listener;
            this.id = nodeIdCounter;
            nodeIdCounter += 1;
        }
        
        TreeNode.NAME = "TreeNode";
        
        TreeNode.ATTRS = {
            children: {
                value:[],
                readOnly:false,
                lazyAdd:false
            }, 
            parent: {
                value:null,
                readOnly:true
            }
        };

        Y.extend(TreeNode, Y.Base, { 
                /**
                * Set the child attribute.
                * Fires a "treeChange" event on this node and its ancestors
                * 
                * @method setChildren
                */
                setChildren: function(val) {
                    this._set( "children", val );
                    for( var i=0; i < val.length; i++ ) {
                        val[i]._set( "parent", this );
                    }
                    //alert( "Firing tree change on node: " + this.id );
                    this.fire( "treeChange", {node:this} );
                    for( var parent = this.get( "parent" ); null != parent; parent = parent.get( "parent" ) ) {
                        parent.fire( "treeChange", { node: this } );
                    }
                    //return val;
                }

            } );
        
        //-----------------------------------------------
        
        /**
         * Controller manages a DOM-based tree widget in a given div
         * 
         * @class TreeController
         * @constructor
         * @param sDivSelector
         * @param config Base config 
         * @extends Base
         */
        function TreeController( sDivSelector, config ) {
            // Invoke Base constructor, passing through arguments
            TreeController.superclass.constructor.apply(this, [ config ] );

            this.sDivSelector = sDivSelector;
            this.selectedPaths = [];
            this.openPaths = [];
            this.state = "new";
            this.wrapper = null;
            this.nodeTemplate = Y.one("#treeNodeTemplate").get( 'text' );
            // Node by id cache
            this.renderedNodes = {};
        }
        
        TreeController.NAME = "TreeController";
        
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
                readOnly:false,
                lazyAdd:false
            }, 
            selectedNodes: {
                value:[],
                lazyAdd:false
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
                var children = parent.get( "children" );
                if( Y.Lang.isUndefined( parentNode ) ) {
                    parentNode = Y.one( "#TreeNode" + parent.id );
                }
                var ul = parentNode.one( "ul.nodeChildren" );
                // First zero out the DOM parent 
                ul.setContent( "" );
                for ( var i=0; i < children.length; ++i ) {
                    var li = Y.Node.create( "<li></li>" );
                    var kid = children[i];
                    var kidNode = uiNodeFactory( kid );
                    li.appendChild( kidNode );
                    ul.appendChild( li );
                    renderChildren( kid, kidNode );
                }
                /* delegate handler registered in render() ...
                parentNode.one( ".treeToggle" ).on( "click",
                    openCloseListener, tree
                    ); */
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
                var treeNode = parent.getData( "littleTreeNode" );
                    //tree.renderedNodes[ parent.get( "id" ) ];
                if ( Y.Lang.isFunction( treeNode.listener ) ) {
                    treeNode.listener( event, treeNode );
                }                
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
                node.set( "id", "TreeNode" + treeNode.id );
                node.one( ".nodeLabel" ).setContent( treeNode.labelHTML ); //+ " " + node.get( "id" ) );
                node.setData( "littleTreeNode", treeNode );
                //tree.renderedNodes[ node.get( "id" ) ] = treeNode;
                return node;
            };
            
            return {
                uiNodeFactory: uiNodeFactory,
                renderChildren: renderChildren,
                openCloseListener: openCloseListener
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
                var internal = TreeControllerInternal( this );
                this.wrapper.delegate( "click",
                    internal.openCloseListener,
                    //function(ev) { ev.preventDefault(); alert( "Click!" ); },
                    "a.treeToggle", this
                    );

                var thisTree = this;
                var root = this.get( "treeModel" );
                var changeHandler = function(e) {
                  internal.renderChildren( e.node );
                };
                this.rootSubscription = root.after( "treeChange", changeHandler );

                this.after( "treeModelChange", function(event) {
                    log.log( "Processing treeModelChange event ..." );
                    this.rootSubscription.detach();
                    this.rootSubscription = event.newVal.after( "treeChange", changeHandler );
                    this.reRender();
                } );
            },
        
            reRender : function() {
                this.renderedNodes = {};
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
                    var toggleModel = new TreeNode( 
                        "/", "<b>/</b>", //[] ),
                        [ new TreeNode( "D" ), new TreeNode( "E", "<b>E</b>", [ new TreeNode( "EEE") ] ), 
                             new TreeNode( "F" ) 
                        ], {}, 
                        function(ev,node) { 
                            alert( "Event on node " + node.id ); 
                            node.listener = null;
                            node.setChildren( [ new TreeNode( "X" ), new TreeNode( "Y" ) ] );
                        }
                        );
                    testButton.delegate( 'click', 
                        function(ev) { 
                            ev.preventDefault();
                            tree.set( "treeModel", toggleModel ); 
                            testButton.setContent( "<div>Toggle root / node to test toggle listener.</div>");
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
    requires: [ 'base', 'node', 'node-base', 'littleware-littleUtil']
});

