package littleware.bootstrap;

import com.google.inject.Module;
import java.util.Optional;

/**
 * Bootstrap module for session-scoped classes
 * instantiated by the child-injector at
 * ClientBoootstrap.startSession() ...
 */
public interface SessionModule extends Module {
    /**
     * Return an optional class to inject and run after session injector initialization -
     * note the run() method should runs inline with session startup,
     * so it should return quickly or it will lock up session startup
     */
    public Optional<? extends Class<? extends Runnable>>  getSessionStarter();    
    
}
