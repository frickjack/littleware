package littleware.apps.swingclient;

/**
 * Interface for handlers/controllers of LittleEvent type events
 * thrown by LittleToo UI elements.
 */
public interface LittleListener extends java.util.EventListener {
	/**
	 * Notify this listener of the occurrence of a LittleEvent type event
	 *
	 * @param event_little that took place
	 */
	public void receiveLittleEvent ( LittleEvent event_little );
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

