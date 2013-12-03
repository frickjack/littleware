/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.servlet.group.controller;

import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.asset.client.AssetRef;

/**
 * Interface underlying user-group web service.
 * Maintains a set of user-defined groups under the littleware.groupapp home asset,
 * but also allows loading of groups with arbitrary id under arbitrary paths.
 */
public interface GroupMgr {
    
    public AssetRef createGroup( String name, Iterable<UUID> memberIds );
    
    public AssetRef loadById( UUID groupId ) throws GeneralSecurityException;
    
    public AssetRef loadAtPath( String groupPath ) throws GeneralSecurityException;
    
    public AssetRef addMembers( UUID groupId, Iterable<UUID> memberIds ) throws GeneralSecurityException;
    
    public AssetRef removeMembers( UUID groupId, Iterable<UUID> memberIds ) throws GeneralSecurityException;
}
