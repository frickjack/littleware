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
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JPanel;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.view.BaseView.ViewBuilder;
import littleware.apps.swingclient.FeedbackBundle;
import littleware.base.swing.GridBagWrap;

/**
 * Simple BaseView.ViewBuilder implementation
 */
public class SimpleViewBuilder implements BaseView.ViewBuilder {
    private BaseData baseData;
    private final List<Action> menuActionList = new ArrayList<Action>();
    private JPanel jcontentPanel;
    private final FeedbackBundle fbBundle;

    private static class View extends BaseView {
        public View( JMenu jtoolMenu,
            JPanel jcontentPanel,
            FeedbackBundle  fbBundle
            ) {
            super( jtoolMenu, jcontentPanel, fbBundle );
        }
    }

    @Inject
    public SimpleViewBuilder( FeedbackBundle fbBundle ) {
        this.fbBundle = fbBundle;
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
    public ViewBuilder contentPanel(JPanel jcontentPanel) {
        this.jcontentPanel = jcontentPanel;
        return this;
    }

    @Override
    public BaseView build() {
        final JMenu jmenu = new JMenu();
        jmenu.setName( "SimpleViewBuilder.toolMenu" );
        for( Action action : menuActionList ) {
            jmenu.add(action);
        }
        final View view = new View( jmenu, jcontentPanel, fbBundle );
        final GridBagWrap gb = GridBagWrap.wrap(view);
        return view;
    }

}
