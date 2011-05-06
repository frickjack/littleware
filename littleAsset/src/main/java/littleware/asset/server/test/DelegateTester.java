/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.test;

import com.google.inject.Inject;
import java.util.logging.Logger;
import junit.extensions.TestDecorator;
import junit.framework.TestResult;
import littleware.asset.server.LittleContext;
import littleware.test.LittleTest;

/**
 * Decorator delegates the test execution to an injected
 * TestCase, but runs its own setUp and tearDown.
 */
public class DelegateTester extends TestDecorator {

    private static final Logger log = Logger.getLogger(DelegateTester.class.getName());
    private final LittleTest child;
    private final AbstractServerTest cleanup;

    @Inject
    public DelegateTester(LittleContext ctx, LittleTest child) {
        super(child);
        this.child = child;
        this.cleanup = new AbstractServerTest(ctx);
    }

    public DelegateTester putName( String name ) {
        child.setName( name );
        return this;
    }

    @Override
    public void run(TestResult result) {
        cleanup.setUp();
        try {
            child.run(result);
        } finally {
            cleanup.tearDown();
        }
    }

    @Override
    public void basicRun(TestResult result) {
        run(result);
    }

    @Override
    public int countTestCases() {
        return child.countTestCases();
    }

    /*
    @Override
    public TestResult run() {
    child.setName(getName());
    return child.run();
    }


    @Override
    public TestResult createResult() {
    return child.createResult();
    }

    @Override
    public void setUp() {
    try {
    super.setUp();
    child.setUp();
    } catch (RuntimeException ex) {
    throw ex;
    } catch (Exception ex) {
    log.log(Level.WARNING, "Setup failed", ex);
    fail("Caught exception: " + ex);
    }
    }

    @Override
    public void tearDown() {
    try {
    child.tearDown();
    } catch (RuntimeException ex) {
    throw ex;
    } catch (Exception ex) {
    log.log(Level.WARNING, "Teardown failed", ex);
    fail("Caught exception: " + ex);
    } finally {
    try {
    super.tearDown();
    } catch (Exception ex) {
    log.log(Level.WARNING, "Teardown failed", ex);
    }
    }
    }


    @Override
    public void runTest() throws Throwable {
    child.runTest();
    }

    @Override
    public void runBare() throws Throwable {
    child.runBare();
    }
     *
     */
}
