package littleware.base;

/**
 * Little RuntimeException thrown by objects that have
 * been set read-only on attempted modification.
 */
public class ReadOnlyException extends UnsupportedOperationException {
	public ReadOnlyException () { super (); }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

