/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient;

import littleware.base.feedback.SimpleLittleTool;
import littleware.base.feedback.LittleListener;
import littleware.base.feedback.LittleTool;
import com.google.inject.Inject;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.UUID;
import javax.swing.*;
import java.util.logging.Logger;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.apps.swingclient.event.*;

/** 
 * Component specializes JList with a JListRenderer
 *
 * TODO: add mouse listener
 */
public class JAssetLinkList extends JPanel implements LittleTool {

    private final static Logger olog_generic = Logger.getLogger(JAssetLinkList.class.getName());
    private static final long serialVersionUID = -56729465687479569L;
    private final SimpleLittleTool otool_handler = new SimpleLittleTool(this);
    private final JLabel ojLabelHeader = new JLabel("");
    private final JList ojList = new JList(new DefaultListModel());


    private final JAssetLinkRenderer orenderer;
    /**
     * Property tracks whether to render the thumbnail or not
     * when available
     */
    public boolean isRenderThumbnail() {
        return orenderer.isRenderThumbnail();
    }
    public void setRenderThumbnail(boolean bRenderThumb) {
        orenderer.setRenderThumbnail( bRenderThumb );
    }



    /**
     * Set the header-label at the top of the label list.
     */
    public String getHeader() {
        return ojLabelHeader.getText();
    }

    /**
     * Set header to html-bold sHeader unless sHeader already
     * starts with an html tag.
     */
    public void setHeader(String sHeader) {
        if (sHeader.startsWith("<html>")) {
            ojLabelHeader.setText(sHeader);
        } else {
            ojLabelHeader.setText("<html><b>" + sHeader + "</b></html>");
        }
    }

    /**
     * Little utility method that by default just calls: <br />
     *     wlink_asset.setLink ( a_view );  <br />
     * , but that a subtype may override to customize for
     * things like an AclEntry where we want to display the
     * entry Principal and list of permissions rather than the
     * AclEntry asset itself.
     *
     * @param wlink_asset widget to setup
     * @param a_view to associate with the wlink_asset widget
     */
    protected void setLink(JAssetLink wlink_asset, Asset a_view) {
        wlink_asset.setLink(a_view);
    }

    /**
     * Inject asset-link cell renderer.
     */
    @Inject
    public JAssetLinkList(
            JAssetLinkRenderer renderer) {
        //super(new GridBagLayout());
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        ojList.setCellRenderer(renderer);
        orenderer = renderer;
        GridBagConstraints grid_control = new GridBagConstraints();

        grid_control.anchor = GridBagConstraints.NORTHWEST;
        grid_control.fill = GridBagConstraints.HORIZONTAL;
        grid_control.gridx = 0;
        grid_control.gridy = 0;
        grid_control.gridheight = 1;
        grid_control.gridwidth = 4;
        grid_control.insets = new Insets(2, 2, 2, 2);
        this.add(ojLabelHeader ); //, grid_control);

        grid_control.gridy += grid_control.gridheight;
        grid_control.fill = GridBagConstraints.BOTH;
        grid_control.gridheight = GridBagConstraints.REMAINDER;
        JScrollPane  jScroll = new JScrollPane( ojList );
        jScroll.setPreferredSize( new Dimension( 200, 200 ) );
        this.add(
                jScroll
                //JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)
                //,grid_control
                );

        MouseListener mouseListener = new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = ojList.locationToIndex(e.getPoint());
                    final Object xGo = getModel().getElementAt(index);
                    if (null == xGo) {
                        return;
                    } else if (xGo instanceof Asset) {
                        otool_handler.fireLittleEvent(new NavRequestEvent(JAssetLinkList.this,
                                ((Asset) xGo).getObjectId(),
                                NavRequestEvent.NavMode.GENERIC));
                    } else if (xGo instanceof UUID) {
                        otool_handler.fireLittleEvent(new NavRequestEvent(JAssetLinkList.this,
                                (UUID) xGo,
                                NavRequestEvent.NavMode.GENERIC));
                    }
                }
            }
        };
        ojList.addMouseListener(mouseListener);
    }



    /**
     * Get the model this view is watching
     */
    public ListModel getModel() {
        return ojList.getModel();
    }

    public void setModel(ListModel model_list) {
        ojList.setModel(model_list);
    }

    @Override
    public void addLittleListener(LittleListener listen_little) {
        otool_handler.addLittleListener(listen_little);
    }

    @Override
    public void removeLittleListener(LittleListener listen_little) {
        otool_handler.removeLittleListener(listen_little);
    }
}
