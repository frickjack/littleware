package littleware.test;

import com.google.inject.Injector;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.LittleBootstrap;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;


/**
 * JUnit5 parameter resolver enabled with littleware.bootstrap Guice bootstrap and
 * injection.
 */
public class LittleParameterResolver implements ParameterResolver {
    private static final Logger log = Logger.getLogger(LittleTestRunner.class.getName());
    
    private static class InjectorSingleton {
        private static final String lock = "lock";
        private static volatile Injector injector;

        public static Injector getInjector() {
            Injector injector = InjectorSingleton.injector;
            if ( null == injector ) {
                synchronized( lock ) {
                    injector = InjectorSingleton.injector;
                    if ( null == injector ) {
                        try {
                            final LittleBootstrap boot = LittleBootstrap.factory.lookup( LittleBootstrap.class );
                            InjectorSingleton.injector = injector = boot.newSessionBuilder().build().startSession( Injector.class );
                        } catch ( Throwable ex ) {
                            log.log( Level.WARNING, "Failed container setup", ex );
                            throw ex;
                        }
                    }
                }
            }
            return injector;
        }

    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return true; 
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return InjectorSingleton.getInjector().getInstance(
            parameterContext.getParameter().getType()
            );
    }
}
