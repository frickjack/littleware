package littleware.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * sql.ResultSet iterator helper
 */
public abstract class AbstractResultSetIterator<T> implements java.util.Iterator<T> {

    private Optional<Boolean> lastNext = Optional.empty();
    private int counter = 0;
    private final static Logger log = Logger.getLogger(AbstractResultSetIterator.class.getName());
    protected final ResultSet rset;

    protected AbstractResultSetIterator(ResultSet rset) {
        this.rset = rset;
    }

    /**
     * Subtypes should implement this converter - assemble T from rset assuming
     * rset.next has already been called
     */
    protected abstract T extract();

    @Override
    public final T next() {
        hasNext();
        lastNext = Optional.empty();
        counter += 1;
        if (0 == counter % 10000) {
            log.log(Level.FINE, "Retrieving row: {0}", counter);
        }
        return extract();
    }

    @Override
    public final boolean hasNext() {
        try {
            if ( ! lastNext.isPresent()) {
                lastNext = Optional.of(rset.next());
            }
            return lastNext.get();
        } catch (SQLException ex) {
            throw new IllegalStateException("Caught checked exception", ex);
        }
    }
}