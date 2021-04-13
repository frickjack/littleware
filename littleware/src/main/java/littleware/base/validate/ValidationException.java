package littleware.base.validate;

import java.util.concurrent.Callable;

/**
 * Thrown on failed validation
 */
public class ValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidationException() {
    }
    public ValidationException( String message ) {
        super( message );
    }
    public ValidationException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Convenience factory method - throws ValidationException if ! mustBeTrue
     *
     * @param mustBeTrue test
     * @param message exception message
     */
    public static void validate( boolean mustBeTrue, String message ) {
        if ( ! mustBeTrue ) {
            throw new ValidationException( message );
        }
    }

    public static void validate( boolean mustBeTrue, Callable<String> messageThunk ) {
        if ( ! mustBeTrue ) {
            String message;
            try {
                message = messageThunk.call();
            } catch ( Exception ex ) {
                message = ex.getMessage();
            }
            throw new ValidationException( message );
        }
    }
}
