/*
 * Copyright 2011 catdogboy at yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


/**
 * littleware.littleUtil module,
 * see http://yuiblog.com/blog/2007/06/12/module-pattern/
 * YUI doc comments: http://developer.yahoo.com/yui/yuidoc/
 * YUI extension mechanism: http://developer.yahoo.com/yui/3/yui/#yuiadd
 *
 * @module littleware
 * @submodule littleUtil
 * @namespace littleware.littleUtil
 */
YUI.add('littleware-littleUtil', function(Y) {
    Y.namespace('littleware');
    Y.littleware.littleUtil = (function() {
        /**
         * Return a test suite to test this submodule
         * @method buildTestSuite
         * @return Y.Test.Suite
         */
        var buildTestSuite = function() {
            var suite = new Y.Test.Suite( "littleware-littleUtil Test Suite");
            suite.add( new Y.Test.Case( {
                name: "Bogus Test",
                testBogus: function() {
                    Y.Assert.isTrue( true, "Do nothing test" );
                }
            }
            ));

            return suite;
        };
        // expose an api
        return {
            buildTestSuite: buildTestSuite
        };
    })();
}, '0.1.1' /* module version */, {
    requires: []
});
