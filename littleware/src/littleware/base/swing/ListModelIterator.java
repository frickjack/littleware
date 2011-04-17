package littleware.base.swing;

import java.util.Iterator;

import javax.swing.ListModel;

/**
 * Convenience class allows iterating over a swing ListModel.
 * Not safe to use this over a changing list though.
 */
public class ListModelIterator implements Iterator<Object> {
    private final ListModel  olmodel;
     
    private int              oi_position = 0;
    
    
    /**
     * Constructor takes the ListModel to iterate over
     *
     * @param lmodel_iterate to iterate over
     */
    public ListModelIterator ( ListModel lmodel_iterate ) {
        olmodel = lmodel_iterate;
    }
    
    public boolean hasNext () {
        return (oi_position < olmodel.getSize ());
    }
    
    public Object next () {
        ++oi_position;
        return olmodel.getElementAt ( oi_position - 1 );
    }
    
    /**
     * Not supported
     *
     * @throws UnsupportedOperationException always thrown
     */
    public void remove () {
        throw new UnsupportedOperationException ();
    }

    
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
