package littleware.bootstrap;

import com.google.inject.Binder;
import com.google.inject.Inject;
import java.util.Optional;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.test.LittleTest;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(LittleTestRunner.class)
public class BootstrapTester {
    private final LittleBootstrap boot;

    @Inject
    public BootstrapTester(LittleBootstrap boot) {
        this.boot = boot;
    }

    /**
     * Just simple test to verify that ClientBootstrap
     * and ServerBootstrap load some modules
     */
    @Test
    public void testModuleLoad() {
        try {
            assertTrue("Found an app module",
                    !boot.getModuleSet().isEmpty());
            assertTrue( "Boootstrap factory initialized",
                    LittleBootstrap.factory.getActiveRuntime().get() == boot
                    );
        } catch (Exception ex) { LittleTest.handle(ex); }
    }

    private SessionBootstrap buildSession(final String label) {
        final SessionBootstrap.SessionBuilder builder = boot.newSessionBuilder();
        for (SessionModuleFactory factory : builder.getSessionModuleSet()) {
            builder.removeModuleFactory(factory);
        }
        builder.addModuleFactory(new SessionModuleFactory() {

            @Override
            public SessionModule buildSessionModule(AppProfile profile) {
                return new SessionModule() {

                    @Override
                    public Optional<? extends Class<Runnable>> getSessionStarter() {
                        return Optional.empty();
                    }

                    @Override
                    public void configure(Binder binder) {
                        binder.bind(String.class).toInstance(label);
                    }
                };
            }
        });
        return builder.build();
    }

    /**
     * Verify semantics of child session builders ...
     */
    @Test
    public void testSessionSemantics() {
        try {
            final String label1 = buildSession("1").startSession(String.class);
            assertTrue("Got expected session 1 label: " + label1, "1".equals(label1));
            final String label2 = buildSession("2").startSession(String.class);
            assertTrue("Got expected session 2 label: " + label2, "2".equals(label2));
        } catch (Exception ex) { LittleTest.handle(ex); }
    }
}
