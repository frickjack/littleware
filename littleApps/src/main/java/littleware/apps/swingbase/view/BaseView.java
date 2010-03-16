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

import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JPanel;
import littleware.apps.swingclient.FeedbackBundle;
import littleware.base.feedback.Feedback;

/**
 *
 * @author pasquini
 */
public abstract class BaseView extends JFrame {
    private final JMenu  jtoolMenu;
    private final JPanel jcontentPanel;
    private final Feedback feedback;

    protected BaseView( JMenu jtoolMenu,
            JPanel jcontentPanel,
            FeedbackBundle  fbBundle
            ) {
        this.jtoolMenu = jtoolMenu;
        this.jcontentPanel = jcontentPanel;
        this.feedback = fbBundle.getFeedback();
    }

    public JMenu getToolMenu() {
        return jtoolMenu;
    }

    public JPanel   getContentPanel() {
        return jcontentPanel;
    }

    public Feedback  getFeedback() {
        return feedback;
    }

    
    public interface ViewBuilder {
        public ViewBuilder  addMenuItem( Action menuItem );
        public ViewBuilder  contentPanel( JPanel jcontentPanel );
        public BaseView  build();
    }
}
