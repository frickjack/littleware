/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;


import littleware.base.AssertionFailedException;


/**
 * Handy LgoCommand baseclass
 */
public abstract class AbstractLgoCommand<Tin,Tout> implements LgoCommand<Tin,Tout> {
    /**
     * Subtypes must initialize the name property.
     * 
     * @param s_name
     */
    protected AbstractLgoCommand( String s_name ) {
        os_name = s_name;
    }
            
    private final String os_name;
    public String getName() {
        return os_name;
    }



    /** NOOP implementation */
    public void processCommandArgs(String[] v_args) throws LgoException {        
    }

    /**
     * Provides a brain-dead implementation that just reads
     * in up to 1024 UTF-8 encoded characters, and calls
     * through to runCommand(string).
     * 
     * @param streamin
     * @param streamout
     * @throws littleware.apps.lgo.LgoException
     */
    public void runCommand( InputStream streamin, OutputStream streamout 
            ) throws LgoException, IOException
    {        
        char[] v_buffer = new char[1024];
        Reader reader = new InputStreamReader( streamin );
        Writer writer = new OutputStreamWriter( streamout, Charset.forName( "UTF-8" ) );
        
        for( int i_read = reader.read( v_buffer );
            i_read >= 0;
            i_read = reader.read( v_buffer )
            ) {
            writer.write( runCommand( new String( v_buffer, 0, i_read ) ) );
        }
    }
            

    /**
     * Abstract implementation just calls through to
     *     runDynamic( s_in ).toString()
     * Override if that is not appropriate for your command.
     * 
     * @param s_in
     * @return
     * @throws littleware.apps.lgo.LgoException
     */
    public String runCommand( String s_in ) throws LgoException
    {
        return runDynamic( s_in ).toString();
    }
    
    public Tout runDynamic ( Object x_in ) throws LgoException {
        return runSafe( (Tin) x_in );
    }
    
    public abstract Tout runSafe( Tin in ) throws LgoException;
    
    /** Subtypes should override via normal clone() rules */
    @Override
    public AbstractLgoCommand clone () {
        try {
            return (AbstractLgoCommand) super.clone();
        } catch ( CloneNotSupportedException e ) {
            throw new AssertionFailedException( "Clone whatever ?", e );
        }
    }
}
