/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.swing;

import javax.swing.AbstractAction;

/**
 * Simple action associates a FileBrowseDialog with a JTextField
 */
import javax.swing.JFileChooser;

import javax.swing.JTextField;

class FileBrowseAction extends AbstractAction {

    private final JTextField jtextField;

    public FileBrowseAction(JTextField jtextField, String label) {
        super(label);
        this.jtextField = jtextField;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent evt) {
        final JFileChooser jdialog = new JFileChooser(jtextField.getText());
        if (jdialog.showOpenDialog(jtextField.getRootPane()) == JFileChooser.APPROVE_OPTION) {
            jtextField.setText(jdialog.getSelectedFile().toString());
        }
    }
}
