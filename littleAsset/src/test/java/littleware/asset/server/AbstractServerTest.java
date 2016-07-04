package littleware.asset.server;

import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleContext.ContextFactory;
import littleware.security.auth.LittleSession;
import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;

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

    public void setUp() {
        this.ctx.getTransaction().startDbAccess();
    }

    public void tearDown() {
        this.ctx.getTransaction().endDbAccess();
        assertTrue( "Test transaction is complete",
                ctx.getTransaction().getNestingLevel() == 0
                );
    }
}
