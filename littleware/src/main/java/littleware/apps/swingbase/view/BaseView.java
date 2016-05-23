/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.view;

import littleware.apps.swingbase.view.internal.SimpleViewBuilder;
import com.google.inject.ImplementedBy;
import java.awt.Container;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPanel;
import littleware.apps.swingbase.controller.ShutdownHandler;
import littleware.base.feedback.Feedback;

/**
 * Standard BaseView JFrame built via ViewBuilder nested interface.
 */
public interface BaseView {

    /**
     * Get the container in which the view is hosted
     */
    public Container getContainer();
    public JMenu getToolMenu();
    public JPanel getBasicContent();
    public Feedback getFeedback();

    @ImplementedBy(SimpleViewBuilder.class)
    public interface ViewBuilder {

        public ViewBuilder addToolMenuItem(Action value);

        public ViewBuilder basicContent(JPanel value);

        /**
         * Override container - default is a JFrame.
         * If value is a JFrame at build time, then will register windowCloseHandler
         * and an Exit menu-item.
         */
        public ViewBuilder container(Container value);

        /**
         * Defaults to ShutdownHandler guice binding - this
         * override is here mostly to fascilitate unit testing
         */
        public ViewBuilder windowCloseHandler(ShutdownHandler value);

        public BaseView build();
    }
}
