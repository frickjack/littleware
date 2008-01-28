package littleware.cookbook;

import java.sql.*;
import corejava.*;
import java.io.PrintWriter;
import littleware.base.*;
import littleware.db.*;

/**
 * ConsoleUI - the console based user interface to the stupid SQL cookbook
 *
 * @author Reuben Pasquini
 */
public class ConsoleUI implements Command {
  Factory  ox_connection_factory = x_connection_factory;

  /**
   * Use case indexes
   */
  public static final int QUIT = 0;
  public static final int QUERY = 1;
  public static final int INSERT = 2;

  private PrintWriter cout;
  private PrintWriter cbug;
  private PrintWriter cdebug;
  private Reader      cin;

  /**
   * Constructor simply caches a source of
   * database connections, and sets the streams
   * that the UI should communicate with.
   *
   * @param x_connection_factory is a source of db connections
   * @param x_out is a writer to write interactive stuff to
   * @param x_debug is a writer to write debug messages to
   * @param x_bug is a writer to write error messages to
   * @param x_in is a reader to read user input from
   */
  public ConsoleUI ( Factory x_connection_factory,
		     PrintWriter  x_out, 
		     PrintWriter  x_bug,
		     PrintWriter  x_debug,
		     Reader       x_in ) {
    ox_connection_factory = x_connection_factory;
    cout = x_out;
    cbug = x_bug;
    cdebug = x_debug;
    cin = x_in;
  }

  /**
   * Writes the main menu to the
   * output stream, then reads the user selection,
   * and returns the index.
   */
  public void main_menu {

    public Main_menu () {}

    public void main_menu () {

    public void doIt () {
      for ( int i_decision = main_menu ();
      while ( QUIT != decision ) {
	cout.print ( "\nMAIN MENU for table " + _table.getName () +
			   "\n\n" +
			   QUIT + ". Quit\n" +
			   QUERY + ". Query\n" +
			   INSERT + ". Insert\n" +
			   NEW_TABLE + ". Create table\n" +
			   SET_TABLE + ". Switch tables\n\n" );
	decision = Console.readInt ( "Your selection please: " );
	
	switch ( decision ) {
	case INSERT: { new Insert_menu ( new Recipe () ).menu (); } break;
	case QUERY: { handle_query (); } break;
	case NEW_TABLE: { 
	  try {
	    _table.create (); 
	  } catch ( SQLException ex ) {
	    cerr.println ( "SQLException: " + ex.getMessage () );
	  }
	} break;
	}
      }
    }
  }
    
  public class Insert_menu {
    public static final int CANCEL = 0;
    public static final int EDIT = 1;
    public static final int INSERT = 2;
    Recipe _recipe;

    public Insert_menu ( Recipe recipe ) {
      Assertion.check ( recipe, "Null recipe passed to Insert_menu" );
      _recipe = recipe;
    }

    /**
     * Collect new user data to insert into the table
     */
    public void menu () {
      int      decision = -1;

      while ( CANCEL != decision ) {
	display ( _recipe );
	cout.print ( "\nINSERT MENU\n\n" +
			   CANCEL + ". Return to MAIN MENU\n" +
			   EDIT + ". Edit recipe\n" +
			   INSERT + ". Insert recipe\n\n" );
	decision = Console.readInt ( "Your selection please: " );
	
	switch ( decision ) {
	case EDIT: {} break;
	case INSERT: {
	  try {
	    _table.insert ( _recipe );
	  } catch ( SQLException ex ) {
	    cerr.println ( "SQLException: " + ex.getMessage () );
	  }
	} break;
	}
      }
    }
  }
    
  /**
   * Print the recipe to the screen 
   */
  public void display ( Recipe recipe ) {
    cout.println ( "1. " + recipe.getName () );
    cout.println ( "2. " + recipe.getDescription () );
    cout.println ( "3. " + recipe.getDate () );
    cout.println ( "4. author - " + recipe.getAuthor () );
    cout.println ( "5. " + recipe.getEmail () );
    cout.println ( "6. vegetarian - " + recipe.getVegetarian () );
    cout.println ( "7. vegan - " + recipe.getVegan () );
    cout.println ( "8. type - " + 
			 Recipe.typeString ( recipe.getType () ) );
    cout.println ( "9. calories - " + recipe.getCalories () );
    cout.println ( "10. prep time - " + recipe.getPreptime () +
			 " minutes" );
    cout.println ( "11. Cost - $" + recipe.getCost () );
    cout.println ( "\n12. Ingredients - \n" +
			 recipe.getIngredients () );
    cout.println ( "\n13. Steps - \n" +
			 recipe.getSteps () );
  }
  
  
  public void enter ( Table table ) {
    _table = (Cookbook) table;
    new Main_menu ().menu ();
  }
  
  public void handle_query () {
  }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.com

