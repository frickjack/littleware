package littleware.base.feedback.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.util.Collection;
import java.util.Iterator;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.FeedbackIterableBuilder;

public class SimpleIterable implements FeedbackIterableBuilder {

    private final Feedback defaultFeedback;

    @Override
    public <T> Iterable<T> build(Iterable<T> wrap, int size) {
        return build( wrap, size, defaultFeedback );
    }

    @Override
    public <T> Iterable<T> build(Iterable<T> wrap, int size, Feedback fbOverride) {
        return new FbIterable<T>( wrap, size, fbOverride);
    }

    private static class FbIterable<T> implements Iterable<T> {

        private final Iterable<T> collection;
        private final int size;
        private final Feedback fb;

        public FbIterable(Iterable<T> collection, int size, Feedback fb) {
            this.collection = collection;
            this.size = size;
            this.fb = fb;
        }

        @Override
        public Iterator<T> iterator() {
            return new FbIterator();
        }

        private class FbIterator implements Iterator<T> {

            private int progress = 0;
            private final Iterator<T> it = collection.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                final T result = it.next();
                fb.setProgress(++progress, size );
                return result;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        }
    }

    @Inject
    public SimpleIterable(Feedback defaultFeedback) {
        this.defaultFeedback = defaultFeedback;
    }

    @Override
    public <T> Iterable<T> build(Collection<T> wrap) {
        return build(wrap, defaultFeedback);
    }

    @Override
    public <T> Iterable<T> build(Collection<T> wrap, Feedback fbOverride) {
        return new FbIterable<T>( ImmutableList.copyOf(wrap), wrap.size(), fbOverride);
    }
}
