/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.controller;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import littleware.apps.swingbase.model.BaseData;
import littleware.apps.swingbase.view.JPropEditor;

/**
 * Display the BaseData properties editor
 */
@Singleton
public class EditOptionsAction extends AbstractAction {
    private static final Logger log = Logger.getLogger( EditOptionsAction.class.getName() );
    
    private final BaseData data;
    private final JPropEditor jeditor;
    private final JFrame jframe = new JFrame("Options Editor");
    private final JButton jsaveButton = new JButton();
    
    @Inject
    public EditOptionsAction(final BaseData data,
            final JPropEditor jeditor,
            final SaveOptionsAction saveAction) {
        super( "Options" );
        this.data = data;
        this.jeditor = jeditor;
        jeditor.setProperties( data.getProperties() );

        jframe.setLayout(new BorderLayout());
        jframe.add(new JLabel("Edit " + data.getAppName() + " Options"), BorderLayout.PAGE_START);
        jframe.add( jeditor, BorderLayout.CENTER );
        final JPanel jbuttonPanel = new JPanel();
        
        jsaveButton.setAction(new AbstractAction() {
            @Override
            public Object getValue(String key) {
                return saveAction.getValue(key);
            }

            @Override
            public void actionPerformed(ActionEvent event) {
                final Map<String,String> props = jeditor.getProperties();
                log.log( Level.FINE, "Updating " + props.size() + " properties" );
                for( Map.Entry<String,String> entry : jeditor.getProperties().entrySet() ) {
                    log.log( Level.FINE, "Setting data property: " + entry.getKey() + " to " + entry.getValue() );
                    data.putProperty(entry.getKey(), entry.getValue() );
                }
                saveAction.actionPerformed(event);
                jframe.setVisible(false);
                jsaveButton.setEnabled(false);
            }
        });
        jeditor.addPropertyChangeListener( new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                log.log( Level.FINE, "Received property-change event!" );
                jsaveButton.setEnabled( true );
            }
        } );
        jbuttonPanel.add(jsaveButton);
        jbuttonPanel.add( new JButton( new AbstractAction( "Cancel" ) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jeditor.setProperties( data.getProperties() );
                jsaveButton.setEnabled(false);
                jframe.setVisible( false );
            }
        } ));
        jframe.add(jbuttonPanel, BorderLayout.PAGE_END );
        jframe.pack();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if ( ! jframe.isVisible() ) {
            if ( data.getProperties().size() > 0 ) {
            jsaveButton.setEnabled( false );
            jeditor.setProperties( data.getProperties() );
            jframe.setVisible( true );
            } else {
                JOptionPane.showMessageDialog(null, "No options registered for " + data.getAppName() );
            }
        }
    }
}
