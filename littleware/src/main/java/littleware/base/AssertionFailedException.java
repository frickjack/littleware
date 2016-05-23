package littleware.base;

/**
 * Error for generic assertion/constraint check failures.
 */
public class AssertionFailedException extends BaseRuntimeException {

  /**
   * Default constructor
   */
  public AssertionFailedException() {
    super("Assertion failed");
  }

  /**
   * With message
   */
  public AssertionFailedException(String s_message) {
    super(s_message);
  }

  /**
   * Chaining exceptions
   */
  public AssertionFailedException(String s_message, Throwable e_cause) {
    super(s_message, e_cause);
  }
}
