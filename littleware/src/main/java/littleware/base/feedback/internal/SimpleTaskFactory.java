package littleware.base.feedback.internal;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.Callable;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.TaskFactory;
import littleware.base.feedback.TaskHandle;

/**
 * Simple implementation of Task Factory interface
 */
public class SimpleTaskFactory implements TaskFactory {

    private final Provider<Feedback> fbFactory;
    private final ListeningExecutorService exec;

    public static class SimpleHandle<T> implements TaskHandle<T> {

        private final Feedback fb;
        private final ListenableFuture<T> future;
        private final Function<Feedback, T> func;

        public SimpleHandle(Feedback fb, ListenableFuture<T> future, Function<Feedback, T> func) {
            this.fb = fb;
            this.future = future;
            this.func = func;
        }

        @Override
        public Function<Feedback, T> getFunction() {
            return func;
        }

        @Override
        public ListenableFuture<T> getFuture() {
            return future;
        }

        @Override
        public Feedback getFeedback() {
            return fb;
        }
    }

    @Inject
    public SimpleTaskFactory(Provider<Feedback> fbFactory,
            ListeningExecutorService exec) {
        this.fbFactory = fbFactory;
        this.exec = exec;
    }

    @Override
    public <T> TaskHandle<T> launchTask(final Function<Feedback, T> func) {
        final Feedback fb = fbFactory.get();
        return new SimpleHandle<>(fb,
                exec.submit(() -> func.apply(fb)), func);
    }
}
