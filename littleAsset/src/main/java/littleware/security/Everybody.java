/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security;


/**
 * Marker interface for special "Everybody" group that
 * every principal is a member of.
 * Both getMembers() and copy() throw UnsupportedOperationException.
 */
public interface Everybody extends LittleGroup {
    
}
