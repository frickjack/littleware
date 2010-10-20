/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.swing;

import java.awt.*;


/**
 * Just a place to stuff some utility functions
 */
public abstract class JUtil {


    /**
     * Climb the getParent() tree until the root is found
     *
     * @param w_validate widget to retrieve root from
     * @return root component
     */
    public static Component findRoot(java.awt.Component w_validate) {
        Component w_root = w_validate;
        for (Component w_search = w_validate.getParent();
                (w_search != null) && (w_search != w_root);
                w_search = w_search.getParent()) {
            w_root = w_search;
        }
        return w_root;
    }
}
