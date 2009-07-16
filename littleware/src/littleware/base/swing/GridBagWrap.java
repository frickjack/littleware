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

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

/**
 * Little utility wrapper around a GridBagConstraint
 * and the Container its associated with.
 */
public class GridBagWrap {
    private final GridBagConstraints ogb = new GridBagConstraints();
    {
        ogb.gridx = 0;
        ogb.gridy = 0;
        ogb.anchor = GridBagConstraints.NORTHWEST;
        ogb.gridheight = 1;
        ogb.gridwidth = 1;
        ogb.insets = new Insets( 2,2,2,2 );
        ogb.fill = GridBagConstraints.NONE;
    }
    private final Container          owcontainer;

    /**
     * Inject the container to set GridBagLayout on and wrap
     * for adding child components.
     * Internal GridBagConstraints initialized to
     * x=0, y=0, width=1, height=1, fill=none
     *
     * @param wcontainer to inject after setting to GridBagLayout
     */
    private GridBagWrap( Container wcontainer ) {
        owcontainer = wcontainer;
        owcontainer.setLayout( new GridBagLayout() );
    }

    /**
     * Static factory returns new GridBagWrap wrapping
     * wcontainer
     *
     * @param wcontainer set to GridBagLayout as side effect
     * @return new GridBagWrap( wcontainer )
     */
    public static GridBagWrap wrap( Container wcontainer ) {
        return new GridBagWrap( wcontainer );
    }

    /** Wrapped container property */
    public Container getContainer () {
        return owcontainer;
    }
    /** Wrapped GridBagConstraints */
    public GridBagConstraints getConstraints() {
        return ogb;
    }

    /** Get/set anchor property */
    public GridBagWrap anchor( int iAnchor ) {
        ogb.anchor = iAnchor;
        return this;
    }
    public int anchor() {
        return ogb.anchor;
    }
    /** Shortcut for anchor( GridBagConstraints.WEST ) */
    public GridBagWrap anchorWest () {
        return anchor( GridBagConstraints.WEST );
    }
    /** Shortcut for anchor( GridBagConstraints.EAST ) */
    public GridBagWrap anchorEast () {
        return anchor( GridBagConstraints.EAST );
    }
    /** Shortcut for anchor( GridBagConstraints.NORTH ) */
    public GridBagWrap anchorNorth () {
        return anchor( GridBagConstraints.NORTH );
    }
    /** Shortcut for anchor( GridBagConstraints.NORTHEAST ) */
    public GridBagWrap anchorNorthEast () {
        return anchor( GridBagConstraints.NORTHEAST );
    }
    /** Shortcut for anchor( GridBagConstraints.NORTHWEST ) */
    public GridBagWrap anchorNorthWest () {
        return anchor( GridBagConstraints.NORTHWEST );
    }
    /** Shortcut for anchor( GridBagConstraints.SOUTHEAST ) */
    public GridBagWrap anchorSouthEast () {
        return anchor( GridBagConstraints.SOUTHEAST );
    }
    /** Shortcut for anchor( GridBagConstraints.SOUTHWEST ) */
    public GridBagWrap anchorSouthWest () {
        return anchor( GridBagConstraints.SOUTHWEST );
    }
    /** Shortcut for anchor( GridBagConstraints.CENTER ) */
    public GridBagWrap anchorCenter () {
        return anchor( GridBagConstraints.CENTER );
    }
    
    /**
     * Get/set internal GridBagConstraints gridx property
     */
    public GridBagWrap gridx( int iX ) {
        ogb.gridx = iX;
        return this;
    }
    public int gridx() { return ogb.gridx; }

    public double weightx() { return ogb.weightx; }
    public GridBagWrap weightx( double dWeight ) {
        ogb.weightx = dWeight;
        return this;
    }

    public double weighty() { return ogb.weighty; }
    public GridBagWrap weighty( double dWeight ) {
        ogb.weighty = dWeight;
        return this;
    }

    /**
     * Get/set internal GridBagConstraints gridx property
     */
    public GridBagWrap gridy( int iY ) {
        ogb.gridy = iY;
        return this;
    }
    public int gridy() { return ogb.gridy; }

    public int fill() { return ogb.fill; }
    public GridBagWrap fill( int iFill ) {
        ogb.fill = iFill;
        return this;
    }

    /** Shortcut for fill( GridBagConstraints.NONE ) */
    public GridBagWrap fillNone() {
        ogb.fill = GridBagConstraints.NONE;
        return this;
    }

    /** Shortcut for fill( HORIZONTAL ) */
    public GridBagWrap fillX() {
        ogb.fill = GridBagConstraints.HORIZONTAL;
        return this;
    }

    /** Shortcut for fill( VERTICAL ) */
    public GridBagWrap fillY() {
        ogb.fill = GridBagConstraints.VERTICAL;
        return this;
    }

    /** Shortcut for fill( BOTH ) */
    public GridBagWrap fillBoth() {
        ogb.fill = GridBagConstraints.BOTH;
        return this;
    }

    public int gridwidth () {
        return ogb.gridwidth;
    }
    public GridBagWrap gridwidth( int iWidth ) {
        ogb.gridwidth = iWidth;
        return this;
    }
    /** Shortcut for gridwidth( GridBagConstraints.REMAINDER ) */
    public GridBagWrap remainderX() {
        return gridwidth( GridBagConstraints.REMAINDER );
    }
    /** Shortcut for gridheight( GridBagConstraints.REMAINDER ) */
    public GridBagWrap remainderY() {
        return gridheight( GridBagConstraints.REMAINDER );
    }

    /** Calls both remainderX and remainderY */
    public GridBagWrap remainderBoth() {
        return remainderX().remainderY();
    }

    public int gridheight () {
        return ogb.gridheight;
    }
    public GridBagWrap gridheight( int iHeight ) {
        ogb.gridheight = iHeight;
        return this;
    }

    /** Next column - shortcut for gridx( gridx + gridwidth ) */
    public GridBagWrap nextCol() {
        return gridx( gridx() + gridwidth() );
    }
    /** Shortcut for gridy( gridy + gridheight ) */
    public GridBagWrap nextRow() {
        return gridy( gridy() + gridheight() );
    }
    /** Shortcut for nextRow().gridx( 0 ) */
    public GridBagWrap newRow() {
        return nextRow().gridx(0);
    }

    private int oiLastX = -1;
    private int oiLastY = -1;
    private final List<Component>   ovCheck = new ArrayList<Component>();

    /**
     * Shortcut for getContainer().add( wcomp, getConstraints() )
     * with a simple check to make sure don't add 2 components
     * at the same position.
     *
     * @return this
     * @exception IllegalArgumentException if attempt to
     *     place component at same position as last addition
     */
     public GridBagWrap add( Component wcomp ) {
         if ( (oiLastX >= 0)
                 && (oiLastY >= 0)
                 && (oiLastX != GridBagConstraints.RELATIVE)
                 && (oiLastY != GridBagConstraints.RELATIVE)
                 && (gridx() != GridBagConstraints.RELATIVE)
                 && (gridy() != GridBagConstraints.RELATIVE)
                 && (gridx() == oiLastX)
                 && (gridy() == oiLastY)
                 )
         {
             throw new IllegalArgumentException ( "Adding overlapping widget at position (" +
                     gridx() + ", " + gridy() + "): " + wcomp.getName()
                     );
         }

         // provide a check that we do not accidentally
         // add the same component more than once
         for ( Component wcheck : ovCheck ) {
             if ( wcheck == wcomp ) {
                 throw new IllegalArgumentException( "Component has already been added to the grid" );
             }
         }
         ovCheck.add( wcomp );
         owcontainer.add( wcomp, ogb );
         oiLastX = gridx();
         oiLastY = gridy();
         return this;
     }
}
