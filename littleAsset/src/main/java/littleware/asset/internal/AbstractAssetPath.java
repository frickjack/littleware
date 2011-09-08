/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.internal;

import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;

import littleware.base.AssertionFailedException;

/**
 * Convenience baseclass for AssetPath implementations 
 */
public abstract class AbstractAssetPath implements AssetPath {

    private static final Logger log = Logger.getLogger("littleware.asset.AbstractAssetPath");
    private String os_subroot_path = null;
    private String os_path = null;
    private AssetPathFactory opathFactory;

    /** 
     * Do-nothing constructor required for java.io.Serializable
     */
    protected AbstractAssetPath() {
    }

    /**
     * Constructor stashes the string path after processing
     * it through AssetFactory.cleanupPath
     *
     * @param path of form /ROOT/A/B/C or whatever
     */
    protected AbstractAssetPath(String path, AssetPathFactory pathFactory) {
        os_path = pathFactory.cleanupPath(path);
        int i_slash = os_path.indexOf("/", 1);
        if (i_slash < 0) {
            os_subroot_path = "";
        } else {
            os_subroot_path = os_path.substring(i_slash);
        }
    }

    @Override
    public boolean hasRootBacktrack() {
        return os_subroot_path.startsWith("/..");
    }

    @Override
    public String getBasename() {
        final int iSlash = os_path.lastIndexOf('/');
        if (iSlash >= 0) {
            if (iSlash + 1 < os_path.length()) {
                return os_path.substring(iSlash + 1);
            } else {
                return "";
            }
        } else {
            return os_path;
        }
    }


    @Override
    public String getSubRootPath() {
        return os_subroot_path;
    }


    @Override
    public boolean hasParent() {
        return os_subroot_path.matches("^(/\\.\\.)*/.?[^\\.].*$");
    }

    /**
     * Relies on clone to assemble the parent path if hasParent(),
     * otherwise just returns this.
     */
    @Override
    public AssetPath getParent() {
        if (hasParent()) {
            try {
                AbstractAssetPath path_result = (AbstractAssetPath) this.clone();
                path_result.os_subroot_path = os_subroot_path.substring(0,
                        os_subroot_path.lastIndexOf("/"));
                path_result.os_path = os_path.substring(0,
                        os_path.lastIndexOf("/"));
                return path_result;
            } catch (StringIndexOutOfBoundsException e) {
                log.log(Level.INFO, "Unexpected " + e + " processing " + os_path +
                        ", " + os_subroot_path);
                throw e;
            }
        } else {
            return this;
        }
    }

    /** Just return constructor-supplied path string */
    @Override
    public String toString() {
        return os_path;
    }

    /** Just hash on toString() */
    @Override
    public int hashCode() {
        return os_path.hashCode();
    }

    /** Just compare on toString () */
    @Override
    public int compareTo(AssetPath path_other) {
        return os_path.compareTo(path_other.toString());
    }

    /** Just call through to super - subclass also needs to implement */
    @Override
    public AbstractAssetPath clone() {
        try {
            return (AbstractAssetPath) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionFailedException("Clone should be supported here", e);
        }
    }

    @Override
    public boolean equals(Object x_other) {
        return ((x_other instanceof AbstractAssetPath) && x_other.toString().equals(this.toString()));
    }
}
