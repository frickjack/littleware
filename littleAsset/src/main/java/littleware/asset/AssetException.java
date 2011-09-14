/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import littleware.base.BaseException;

/**
 * Base Asset exception
 */
public abstract class AssetException extends BaseException {

    /** Goofy default constructor */
    public AssetException() {
        super("Asset system exception");
    }

    /** Constructor with user-supplied message */
    public AssetException(String message) {
        super(message);
    }

    /** Propagating constructor */
    public AssetException(String message, Throwable cause) {
        super(message, cause);
    }
}
