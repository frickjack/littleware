/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.io.File;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.base.BaseException;
import littleware.base.Maybe;


/**
 * Manages the bucket storage for remotely stored member data
 */
public interface ProductManager {
    public void checkout(
            UUID memberId, File destinationFolder
            ) throws BaseException, GeneralSecurityException, RemoteException;



    /**
     * Create a new member under the given version with the contents
     * of the given source file or folder compressed into a .zip file.
     */
    public Member checkin(
            UUID versionId, String memberName, File source, String comment
            ) throws BaseException, GeneralSecurityException, RemoteException;

    /**
     * Get the content-index of the given member
     */
    public MemberIndex getIndex( UUID memberId ) throws BaseException, GeneralSecurityException, RemoteException;

    /*.. Versioning utilities ... ugh.
     * public String nextVersion( String lastVersion, int dotNumber )
     * public String highestVersion( UUID product, String prefix );
     * public Comparator<String> getSimpleVersionSorter();
     * public Maybe<File> locate( UUID memberId ); -- workspace locate ? ...
     */
}
