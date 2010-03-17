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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import littleware.apps.swingbase.controller.ShutdownHandler;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.view.BaseView.ViewBuilder;
import littleware.apps.swingclient.FeedbackBundle;
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
    private final ShutdownHandler shutdownHandler;

    private static class View extends BaseView {
        public View( JMenu jtoolMenu,
            JPanel jcontentPanel,
            FeedbackBundle  fbBundle
            ) {
            super( jtoolMenu, jcontentPanel, fbBundle );
        }
    }

    @Inject
    public SimpleViewBuilder( FeedbackBundle fbBundle,
            LittleBootstrap bootstrap,
            ShutdownHandler shutdownHandler
            ) {
        this.fbBundle = fbBundle;
        this.bootstrap = bootstrap;
        this.shutdownHandler = shutdownHandler;
    }

    @Override
    public ViewBuilder model(BaseData value) {
        this.baseData = value;
        return this;
    }

    @Override
    public ViewBuilder addToolMenuItem(Action menuItem) {
        menuActionList.add( menuItem );
        return this;
    }

    @Override
    public ViewBuilder basicContent(JPanel jcontentPanel) {
        this.jcontentPanel = jcontentPanel;
        return this;
    }

    @Override
    public BaseView build() {
        final JMenu jmenu = new JMenu();
        jmenu.setIcon( new ImageIcon( Thread.currentThread().getContextClassLoader().getResource( "littleware/apps/swingbase/resources/Wrench_32.png" )));
        jmenu.setName( "SimpleViewBuilder.toolMenu" );
        for( Action action : menuActionList ) {
            jmenu.add(action);
        }
        final JPanel menuPanel = new JPanel();
        menuPanel.setName( "SimpleViewBuilder.menuPanel" );
        menuPanel.setLayout( new FlowLayout( FlowLayout.RIGHT ));
        menuPanel.add(jmenu);
        final View view = new View( jmenu, jcontentPanel, fbBundle );
        final GridBagWrap gb = GridBagWrap.wrap(view);
        gb.fillX().remainderX().add(menuPanel).newRow();
        gb.fillBoth().gridheight( 10 ).add( jcontentPanel ).newRow();
        gb.gridheight(1).add( fbBundle.getProgress() ).newRow();
        gb.gridheight( 4 ).fillBoth().add( fbBundle.getText() );
        view.setPreferredSize( new Dimension( 500, 500 ));
        view.pack();
        view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        view.addWindowListener(
                new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent evt) {
                shutdownHandler.requestShutdown();
            }
        }
                );
        return view;
    }

}
