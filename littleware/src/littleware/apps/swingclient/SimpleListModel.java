package littleware.apps.swingclient;

import java.util.List;
import java.util.ArrayList;

import javax.swing.AbstractListModel;

/**
 * Little wrapper around java.util.List to adapt it for use as a ListModel.
 * Requires that clients call fireChangeEvent() after changing the
 * wrapped list to notify listeners of the model change.
 * Listeners are always notified via contentsChanged over the whole
 * list.  The getElement(), etc. methods actually access a copy of
 * the wrapped list that is updated by fireChangeEvent() to
 * avoid issues where a work thread is modifying the list while a UI
 * thread is scanning it.
 */
public class SimpleListModel<T> extends AbstractListModel {
    private final  List<T>  ov_internal;
    private final  List<T>  ov_copy = new ArrayList<T> ();
    
    /**
     * Inject the list to wrap
     */
    public SimpleListModel ( List<T> v_wrap ) {
        ov_internal = v_wrap;
        ov_copy.addAll ( v_wrap );
    }
    
    /**
     * Get the wrapped list for editing
     */
    public List<T> getList () { return ov_internal; }
    
    /**
     * Notify listeners of generic list change
     */
    public synchronized void fireChangeEvent () {
        ov_copy.clear ();
        ov_copy.addAll ( ov_internal );
        fireContentsChanged ( this, 0, ov_copy.size () );
    }
    
    public synchronized T getElementAt ( int i_index ) {
        return ov_copy.get( i_index );
    }
    
    public synchronized int getSize () {
        return ov_copy.size ();
    }

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

