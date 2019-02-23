package littleware.asset;

import littleware.asset.LittleContext.ContextFactory;
import littleware.security.auth.LittleSession;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;

/**
 * TestCase base class sets up and completes
 * injected LittleContext transaction in setup and teardown methods.
 */
public class ServerTestBase  {
    private final LittleContext ctx;

    protected ServerTestBase( LittleSession session, ContextFactory ctxFactory ) {
        this.ctx = ctxFactory.build( session.getId() );
    }

    protected ServerTestBase( LittleContext ctx ) {
        this.ctx = ctx;
    }

    public LittleContext getContext() {
        return ctx;
    }

    @Before
    public void setUp() {
        this.ctx.getTransaction().startDbAccess();
    }

    @After
    public void tearDown() {
        this.ctx.getTransaction().endDbAccess();
        assertTrue( "Test transaction is complete",
                ctx.getTransaction().getNestingLevel() == 0
                );
    }
}
