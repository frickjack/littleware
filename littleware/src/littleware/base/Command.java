package littleware.base;

/**
 * Command pattern interface
 */
public interface Command <R,P> extends java.security.PrivilegedExceptionAction {
	
	/**
	 * Do the command
	 *
	 * @param x_param if a parameter is required for the thing
	 * @return result-type instance
	 */
    public R doIt ( P x_param ) throws Exception;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

