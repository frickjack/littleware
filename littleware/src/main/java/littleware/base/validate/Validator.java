package littleware.base.validate;

import java.util.Collection;

/**
 * Standard interface for validation test
 */
public interface Validator {
    /**
     * @throws ValidateException on validation failure
     */
    public void validate() throws ValidationException;
    /**
     * Returns collection of messages describing on or more invalid conditions,
     * or empty collection if validation passes ok
     * 
     * @return empty message collection on validation success, one or messages on validation failure
     */
    public Collection<String> checkIfValid();

}
