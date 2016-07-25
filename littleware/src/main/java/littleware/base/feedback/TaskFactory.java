package littleware.base.feedback;

import com.google.common.base.Function;

/**
 * Factory launches a given function on an internal
 * executor service with feedback input associated
 * with the appropriate UI element.
 */
public interface TaskFactory {
    public <T> TaskHandle<T> launchTask( Function<Feedback,T> func );
}
