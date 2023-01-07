package littleware.bootstrap;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.inject.Binder;
import com.google.inject.Inject;
import java.util.Optional;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.test.LittleParameterResolver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


@ExtendWith(LittleParameterResolver.class)
public class Junit5ProviderTester {
    private final LittleBootstrap boot;

    @Inject
    public Junit5ProviderTester(LittleBootstrap boot) {
        this.boot = boot;
    }

    /**
     * Just simple test to verify that ClientBootstrap
     * and ServerBootstrap load some modules
     */
    @Test
    public void testModuleLoad() {
        assertTrue(!boot.getModuleSet().isEmpty(), "Found an app module");
        assertTrue(LittleBootstrap.factory.getActiveRuntime().get() == boot, "Boootstrap factory initialized");
    }

}
