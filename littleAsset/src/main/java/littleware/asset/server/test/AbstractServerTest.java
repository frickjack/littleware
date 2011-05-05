/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server.test;

import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleContext.ContextFactory;
import littleware.security.auth.LittleSession;
import littleware.test.LittleTest;

/**
 * TestCase base class sets up and completes
 * injected LittleContext transaction in setup and teardown methods.
 */
public class AbstractServerTest extends LittleTest {
    private final LittleContext ctx;

    protected AbstractServerTest( LittleSession session, ContextFactory ctxFactory ) {
        this.ctx = ctxFactory.build( session.getId() );
    }

    protected AbstractServerTest( LittleContext ctx ) {
        this.ctx = ctx;
    }

    public LittleContext getContext() {
        return ctx;
    }

    @Override
    public void setUp() {
        this.ctx.getTransaction().startDbAccess();
    }

    @Override
    public void tearDown() {
        this.ctx.getTransaction().endDbAccess();
        assertTrue( "Test transaction is complete",
                ctx.getTransaction().getNestingLevel() == 0
                );
    }
}
