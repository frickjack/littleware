package littleware.security.auth.server;


/**
 * Minor specialization of SimpleDbPasswordManager -
 * default constructor specifies super(true) to
 * enable database password check
 */
public class PasswordDbLoginModule extends SimpleDbLoginModule {
	/**
	 * Default constructor enables password-check in base class
	 */
	public PasswordDbLoginModule () { 
		super( true );
	}
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

