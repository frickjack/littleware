package littleware.base.feedback;

import littleware.base.feedback.internal.SimpleIterable;
import com.google.inject.ImplementedBy;
import java.util.Collection;

/**
 * Setup Iterable wrapper around collection where the
 * iterable's iterator advances the given Feedback progress bar
 */
@ImplementedBy(SimpleIterable.class)
public interface FeedbackIterableBuilder {
    /**
     * Return iterable connected to Guice-injected Feedback implementation
     */
    public <T> Iterable<T> build( Collection<T> wrap );
    /**
     * Client must provide size of collection iterable references for
     * feedback to provide useful info
     */
    public <T> Iterable<T> build( Iterable<T> wrap, int size );

    public <T> Iterable<T> build( Collection<T> wrap, Feedback fbOverride );
    public <T> Iterable<T> build( Iterable<T> wrap, int size, Feedback fbOverride );
}
