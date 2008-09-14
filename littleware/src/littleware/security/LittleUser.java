package littleware.security;

/**
 * Specialization of LittlePrincipal with a few
 * more user specific methods not available on groups.
 */
public interface LittleUser extends LittlePrincipal {

    /**
     * Little principal-status class
     */
    public static enum Status {

        ACTIVE,
        INACTIVE
    }

    /** Maps getValue() to a UserStatus */
    public Status getStatus();

    /** Maps to setValue() */
    public void setStatus(Status n_status);

    /** Covariant return-type clone */
    public LittleUser clone();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

