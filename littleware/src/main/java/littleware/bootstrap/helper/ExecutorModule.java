package littleware.bootstrap.helper;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Scopes;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.feedback.TaskFactory;
import littleware.base.feedback.internal.SimpleTaskFactory;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;

/**
 * Littleware module binds, starts, and shuts down ExecutorService thread pool.
 */
public class ExecutorModule extends AbstractAppModule {

    private static final Logger log = Logger.getLogger(ExecutorModule.class.getName());

    public static class Factory implements AppModuleFactory {

        @Override
        public AppModule build(AppProfile profile) {
            return new ExecutorModule(profile);
        }
    }

    private ExecutorModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    /**
     * Utility activator takes care of shutting down the executor service and
     * the JCS cache, and bootstraps the session helper. Public for guice-no_aop
     * access only.
     */
    public static class ExecActivator implements AppModule.LifecycleCallback {

        private final ExecutorService executor;
        private final ScheduledExecutorService scheduledExecutor;

        @Inject
        public ExecActivator(ExecutorService executor,
                ScheduledExecutorService scheduledExecutor) {
            this.executor = executor;
            this.scheduledExecutor = scheduledExecutor;
        }

        @Override
        public void startUp() {
        }

        @Override
        public void shutDown() {
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                log.log(Level.WARNING, "Exception shutting down executor service", ex);
            }
            try {
                if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    scheduledExecutor.shutdownNow();
                }
            } catch (InterruptedException ex) {
                log.log(Level.WARNING, "Exception shutting down executor service", ex);
            }

        }
    }

    @Override
    public void configure(Binder binder) {
        final ScheduledExecutorService schedExec = Executors.newScheduledThreadPool(2);
        final ExecutorService exec = Executors.newFixedThreadPool(10);
        binder.bind(ExecutorService.class).toInstance(exec);
        binder.bind(ScheduledExecutorService.class).toInstance(schedExec);
        binder.bind(ListeningExecutorService.class).toInstance(MoreExecutors.listeningDecorator(exec));
        binder.bind(ListeningScheduledExecutorService.class).toInstance(MoreExecutors.listeningDecorator(schedExec));
        binder.bind(TaskFactory.class).to(SimpleTaskFactory.class).in(Scopes.SINGLETON);
    }

    @Override
    public Optional<Class<ExecActivator>> getCallback() {
        return Optional.of(ExecActivator.class);
    }
}
