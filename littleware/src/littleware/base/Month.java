package littleware.base;

import java.util.Map;
import java.util.HashMap;


/**
 * Convenience Month enumeration.
 * Integrate with ResourceBundle to internationalize later.
 */
public enum Month {
    January, February, March, April, May, June, July,
    August, September, October, November, December;
    
    private static final Map<String,Month>  ov_months = new HashMap<String,Month> ();
    static {
        for ( Month n_month : values () ) {
            ov_months.put ( n_month.toString ().toLowerCase (), n_month );
        }
    }
    
    /**
     * Little utility to lookup a Month by name
     *
     * @param s_english month name to lookup, case insensitive
     * @return Month matching name or null if no match
     */
    public static Month lookupByName ( String s_english ) {
        return ov_months.get ( s_english.toLowerCase () );
    }
};


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
