package littleware.base.feedback;

import com.google.common.base.Function;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Handle returned by TaskFactory
 */
public interface TaskHandle<T> {
    public Function<Feedback,T> getFunction();
    public ListenableFuture<T>  getFuture();
    public Feedback             getFeedback();
}
