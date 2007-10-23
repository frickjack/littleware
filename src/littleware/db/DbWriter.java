package littleware.db;

import java.sql.*;

/**
 * Interface standardizing pattern for saving an object
 * to a database.
 * Idea is that different writers may leverage each other under the hood -
 * so can write a writer that saves a list of objects that uses
 * a writer that saves an individual object. 
 */
public interface DbWriter<T> {
	
	/**
	 * Save an object to this writer's internal DataSource
	 *
	 * @param x_object to save
	 * @exception SQLException on SQL interaction failures - REFACTOR later
	 */
	public void saveObject ( T x_object ) throws SQLException;
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

