/* 
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

/**
 * littleware.feedback.view module provides HTML widgets that
 * view the feedback and task models in littleware.feedback.model.
 * Typical use:  
 * <ul>
 *   <li>var taskBar = new TaskBar();
 *   <li>var task = taskBar.newTask( {}, "run test" );
 *   <li>task.run( function( ctx, fb ) { fb.progress( 50 ).message( "bla bla bla" ) } )
 * </ul>
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware.littleFeedback
 * @namespace littleware.littleFeedback
 */
YUI.add( 'littleware-feedback-view', function(Y) {
    Y.namespace('littleware.feedback');
    Y.littleware.feedback.view = (function() {
        //---------------------------------------
        
        return {
        };
    })();
}, '0.1.1' /* module version */, {
    requires: [ 'io-base', 'node', 'node-base', 'littleware-littleUtil', 'test']
});



