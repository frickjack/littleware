/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.view;

import com.google.inject.Inject;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import littleware.apps.swingbase.controller.ExitAction;
import littleware.apps.swingbase.controller.HelpAction;
import littleware.apps.swingbase.controller.ShutdownHandler;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.view.BaseView.ViewBuilder;
import littleware.apps.swingclient.FeedbackBundle;
import littleware.base.feedback.Feedback;
import littleware.base.swing.GridBagWrap;
import littleware.security.auth.LittleBootstrap;

/**
 * Simple BaseView.ViewBuilder implementation
 */
public class SimpleViewBuilder implements BaseView.ViewBuilder {

    private BaseData baseData;
    private final List<Action> menuActionList = new ArrayList<Action>();
    private JPanel jcontentPanel;
    private final FeedbackBundle fbBundle;
    private final LittleBootstrap bootstrap;
    private ShutdownHandler shutdownHandler;
    private Container rootContainer = new JFrame();
    private final ExitAction exitAction;
    private final HelpAction helpAction;

    @Override
    public ViewBuilder container(Container value) {
        rootContainer = value;
        return this;
    }

    private static class View implements BaseView {

        private final JMenu jtoolMenu;
        private final JPanel jcontentPanel;
        private final Feedback feedback;
        private final Container rootContainer;

        public View(
                Container rootContainer,
                JMenu jtoolMenu,
                JPanel jcontentPanel,
                FeedbackBundle fbBundle) {
            this.jtoolMenu = jtoolMenu;
            this.jcontentPanel = jcontentPanel;
            this.feedback = fbBundle.getFeedback();
            this.rootContainer = rootContainer;
        }

        @Override
        public JMenu getToolMenu() {
            return jtoolMenu;
        }

        @Override
        public JPanel getBasicContent() {
            return jcontentPanel;
        }

        @Override
        public Feedback getFeedback() {
            return feedback;
        }

        @Override
        public Container getContainer() {
            return rootContainer;
        }
    }

    @Inject
    public SimpleViewBuilder(FeedbackBundle fbBundle,
            LittleBootstrap bootstrap,
            ShutdownHandler shutdownHandler,
            HelpAction helpAction,
            ExitAction exitAction) {
        this.fbBundle = fbBundle;
        this.bootstrap = bootstrap;
        this.shutdownHandler = shutdownHandler;
        this.exitAction = exitAction;
        this.helpAction = helpAction;
    }

    @Override
    public ViewBuilder model(BaseData value) {
        this.baseData = value;
        return this;
    }

    @Override
    public ViewBuilder addToolMenuItem(Action menuItem) {
        menuActionList.add(menuItem);
        return this;
    }

    @Override
    public ViewBuilder basicContent(JPanel jcontentPanel) {
        this.jcontentPanel = jcontentPanel;
        return this;
    }

    @Override
    public ViewBuilder windowCloseHandler(ShutdownHandler value) {
        this.shutdownHandler = value;
        return this;
    }

    @Override
    public BaseView build() {
        final JMenu jmenu = new JMenu();
        jmenu.setIcon(new ImageIcon(Thread.currentThread().getContextClassLoader().getResource("littleware/apps/swingbase/resources/Wrench-32.png")));
        jmenu.setName("SimpleViewBuilder.toolMenu");
        for (Action action : menuActionList) {
            jmenu.add(action);
        }
        jmenu.add( helpAction );
        final JPanel menuPanel = new JPanel();
        final JPanel feedbackPanel = new JPanel();
        final Color backgroundColor = new Color(137, 197, 230);

        {
            menuPanel.setBorder(BorderFactory.createRaisedBevelBorder());
            menuPanel.setBackground(backgroundColor);
            menuPanel.setName("SimpleViewBuilder.menuPanel");
            menuPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
            menuPanel.add(new JLabel(baseData.getAppName() + " " + baseData.getVersion()));
            final JMenuBar jmenuBar = new JMenuBar();
            //jmenuBar.setBackground(backgroundColor);
            jmenuBar.setOpaque(false);
            jmenuBar.add(jmenu);
            menuPanel.add(jmenuBar);
        }

        {
            feedbackPanel.setLayout(new BoxLayout(feedbackPanel, BoxLayout.Y_AXIS));
            feedbackPanel.setBackground(backgroundColor);
            feedbackPanel.setBorder(BorderFactory.createMatteBorder(5, 2, 10, 2, backgroundColor));
            feedbackPanel.add(fbBundle.getProgress());
            fbBundle.getText().setRows(5);
            feedbackPanel.add(new JScrollPane(fbBundle.getText(),
                    JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));

        }

        jcontentPanel.setBorder(BorderFactory.createMatteBorder(
                10, 5, 20, 5, jcontentPanel.getBackground()));

        final View view = new View(rootContainer, jmenu, jcontentPanel, fbBundle);
        if (false) { // gb layout
            final GridBagWrap gb = GridBagWrap.wrap(rootContainer).fillNone();
            gb.anchorNorth().fillX().remainderX().add(menuPanel).newRow();
            gb.anchorCenter().fillBoth().add(jcontentPanel).newRow();
            gb.anchorSouth().fillBoth().add(feedbackPanel);
        } else {
            rootContainer.setLayout(new BorderLayout());
            rootContainer.add(menuPanel, BorderLayout.PAGE_START);
            rootContainer.add(jcontentPanel, BorderLayout.CENTER);
            rootContainer.add(feedbackPanel, BorderLayout.PAGE_END);
        }
        //gb.gridheight(4).fillBoth().add(fbBundle.getText());

        //rootContainer.setPreferredSize(new Dimension(800, 800));
        if (rootContainer instanceof JFrame) {
            final JFrame jframe = (JFrame) rootContainer;
            jmenu.add(exitAction);
            jframe.pack();
            jframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            jframe.addWindowListener(
                    new WindowAdapter() {

                        @Override
                        public void windowClosing(WindowEvent evt) {
                            shutdownHandler.requestShutdown();
                        }
                    });
        }





        return view;
    }
}
