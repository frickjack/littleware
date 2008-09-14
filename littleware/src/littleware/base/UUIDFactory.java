package littleware.base;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;


/**
 * Goofy base class for UUID facotires provides a couple utilities for
 * converting UUID's back and forth between strings,
 * but skipping the dashes that are part of the official spec.
 */
public abstract class UUIDFactory implements Factory<UUID>, java.io.Serializable {
	private static Logger olog_generic = Logger.getLogger ( "littleware.base.UUIDFactory" );
	
	/** 
	 * Add '-' to s_uuid if necessary, then call through
	 * to UUID.fromString
	 *
	 * @param s_uuid string to derive UUID from
	 * @return parsed UUID or null if s_uuid is null
	 * @exception IllegalArgumentException if s_uuid is not null and is not parsable
	 */
	public static UUID parseUUID ( String s_uuid ) throws IllegalArgumentException {
		if ( null == s_uuid ) {
			return null;
		}
		if ( (0 > s_uuid.indexOf ( '-' ))
			 && (32 == s_uuid.length ())
			 ) {
			s_uuid = s_uuid.substring ( 0, 8 ) + "-" 
				+ s_uuid.substring ( 8, 12 ) + "-" 
			    + s_uuid.substring ( 12, 16 ) + "-" 
			    + s_uuid.substring ( 16, 20 ) + "-" 
			    + s_uuid.substring ( 20 );
		}
		
		return UUID.fromString ( s_uuid );
	}
	
	
	/**
	 * Convenience function to return a UUID string representation
	 * with every '-' removed, and converted to UPPER case.
	 * That's how we like to store things in the database.
	 *
	 * @return clean uuid string, or null if u_id is null
	 */
	public static String makeCleanString ( UUID u_id ) {
		if ( null == u_id ) {
			return null;
		}
		return u_id.toString ().replaceAll ( "-", "" ).toUpperCase ();
	}
	
    private static UUIDFactory  factory_real = new UUIDFactory () {
        /**
         * Just call through to UUID.randomUUID()
         */
        public synchronized UUID create () {
            return UUID.randomUUID ();
        }
    };
    
    /**
     * Get the current factory implementation
     */
    public static UUIDFactory getFactory () {
        return factory_real;
    }
    
    /** Get a new UUID */
	public abstract UUID create ();
    
    
	
	/** Do-nothing op */
	public void recycle ( UUID u_recycle ) {}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

