/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;


import javax.swing.JComponent;

/**
 * Package local class supports
 * laying out UI rows that consist
 * of a name label and a widget that
 * presents or edits info.
 */
public class NameWidget<T extends JComponent> {

    private final String osName;
    private final T      owComp;

    public NameWidget(String sName, T wComp) {
        osName = sName;
        owComp = wComp;
    }

    public String getName() {
        return osName;
    }

    public T getComp() {
        return owComp;
    }
}
