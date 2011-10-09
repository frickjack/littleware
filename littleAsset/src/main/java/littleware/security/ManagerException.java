package littleware.security;

import java.security.GeneralSecurityException;

/**
 * Group-manipulation/access support exception
 */
public abstract class ManagerException extends GeneralSecurityException {

    /** Default constructor */
    public ManagerException() {
        super("Manager manipulation exception");
    }

    /** Constructor with message */
    public ManagerException(String message) {
        super(message);
    }

    /** Constructor with message and cause */
    public ManagerException(String message, Throwable cause) {
        super(message, cause);
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

