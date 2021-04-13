package littleware.base.validate;

import java.util.Collection;
import littleware.base.Whatever;


/**
 * Base implementation for a compound validator
 */
public abstract class AbstractValidator implements Validator {
    @Override
    public final void validate() throws ValidationException {
        final Collection<String> errors = checkIfValid();
        if ( ! errors.isEmpty() ) {
            final StringBuilder sb = new StringBuilder();
            for( String message : errors ) {
                sb.append( message ).append( Whatever.NEWLINE );
            }
            throw new ValidationException( sb.toString() );
        }
    }

    protected ValidatorUtil.Helper buildErrorTracker() {
        return ValidatorUtil.helper();
    }
}
