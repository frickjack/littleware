package littleware.browser;


/**
 * Interface for manipulating a browser view
 */
public class BrowserView {

	public void clearView ();
	public void updateView ( String s_subtree_url ) throws BrowserException;
	public List<Asset> getSelection ();
	public void setSelection ( String s_url ) throws BrowserException;
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

