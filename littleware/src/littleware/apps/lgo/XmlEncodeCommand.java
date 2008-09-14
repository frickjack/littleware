/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import littleware.base.AssertionFailedException;
import littleware.base.XmlSpecial;


/**
 * XML encode/decode LgoCommand
 */
public class XmlEncodeCommand extends AbstractLgoCommand<String,String> {
    private static final Logger olog = Logger.getLogger( XmlEncodeCommand.class.getName () );
    
    /**
     * Constructor just sets the command-name to this.class.getName,
     * and the command-help property to the supplied help object.
     */
    public XmlEncodeCommand() {
        super( XmlEncodeCommand.class.getName() );        
    }
    
    public enum SubCommand {
        encode, decode, version, help;
    }
    
    private SubCommand on_subcommand = SubCommand.encode;
    
    /**
     * Subcommand property - may also be set via processArgs
     */
    public SubCommand getSubCommand () { 
        return on_subcommand;
    }
    public void setSubCommand( SubCommand n_subcommand ) {
        on_subcommand = n_subcommand;
    }

    private static ResourceBundle obundle_support = null; 
    private static LongOpt[]      ov_longopts = null;
    
    /**
     * Reset the XmlEncodeCommand properties based on the
     * command-line arguments.
     * 
     * @param vArgs
     */
    @Override
    public void processCommandArgs( String[] v_argv ) throws LgoException {
        if ( null == obundle_support ) {
            obundle_support = ResourceBundle.getBundle( XmlEncodeCommand.class.getName() + "Resources" );
        }
        if ( null == ov_longopts ) {
            // create the command line options that we are looking for
            ov_longopts = new LongOpt[] {
                new LongOpt(obundle_support.getString("version.option"), LongOpt.NO_ARGUMENT, null, 1),
                new LongOpt(obundle_support.getString("encode.option"), LongOpt.NO_ARGUMENT, null, 'e'),
                new LongOpt(obundle_support.getString("decode.option"), LongOpt.NO_ARGUMENT, null, 'd')
            };
        }
        final Getopt opts = new Getopt(obundle_support.getString("appname"), 
                v_argv, "ed", ov_longopts
                );

        boolean b_encode = true;

        for (int i_opt = opts.getopt();
                i_opt != -1;
                i_opt = opts.getopt()) {
            switch (i_opt) {
                case 1:
                    on_subcommand = SubCommand.version;
                    break;
                case 'd':
                    on_subcommand = SubCommand.encode;
                    break;
                case 'e':
                    on_subcommand = SubCommand.encode;
                    break;
                default: 
                    throw new LgoBadArgumentException( "Illegal option, try -h for help" );
            }
        }
    }            
    
    
    
    /*... bla bla
    @Override
    public void runCommand( 
            InputStream streamin, 
            OutputStream streamout 
            ) throws LgoException 
    {
        BufferedReader reader = new BufferedReader( new InputStreamReader( streamin ) );
        Writer   writer = new OutputStreamWriter( streamout ); 

        String[] v_argv = this.getCommandArgs();               

        final Getopt opts = new Getopt(bundle_support.getString("appname"), v_argv, "ed", v_longopts);

        boolean b_encode = true;

        for (int i_opt = opts.getopt();
                i_opt != -1;
                i_opt = opts.getopt()) {
            switch (i_opt) {
                case 1:
                     {
                        writer.println("littleware.base.XmlSecial version 0.1");
                        return;
                    }
                    // unreachable - break;

                case 'd':
                     {
                        b_encode = false;
                    }
                    break;
                case 'e':
                    break;
                default: {
                    System.err.println("Illegal option, try -h for help");
                    System.exit(1);
                }
            }
        }
        String s_source = "stdin";
        if (opts.getOptind() < v_argv.length) {
            s_source = v_argv[opts.getOptind()].toLowerCase();
        }
        if (s_source.equals("stdin")) {
            try {
                for (String s_data = reader.readLine();
                        s_data != null;
                        s_data = reader.readLine()) {
                    if (b_encode) {
                        writer.write( XmlSpecial.encode(s_data));
                    } else {
                        writer.write( XmlSpecial.decode(s_data));
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException("Failure reading stdin", e);
            }
        } else { // clipboard
            Clipboard clip_master = Toolkit.getDefaultToolkit().getSystemClipboard();
            try {
                Transferable transfer_in = clip_master.getContents(null);
                String s_data = (String) transfer_in.getTransferData(DataFlavor.stringFlavor);
                olog.log(Level.FINE, "Got from clipboard: " + s_data);
                if (null != s_data) {
                    final String s_out;
                    if (b_encode) {
                        s_out = XmlSpecial.encode(s_data);
                    } else {
                        s_out = XmlSpecial.decode(s_data);
                    }
                    Transferable transfer_string = new StringSelection(s_out);
                    clip_master.setContents(transfer_string, null);
                } else {
                    olog.log(Level.FINE, "null clipboard contents");
                }
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new RuntimeException("Failed clipboard read/write", e);
            }
        }        
    }            
    */
    
    public String runCommand( String s_in ) {
        // Need to add ResourceBundle stuff ...
        switch( on_subcommand ) {
            case version:
                return this.getClass().getName () + " version 0.0";
            case encode:
                return XmlSpecial.encode(s_in);
            case decode:
                return XmlSpecial.decode(s_in);
            default:
                throw new AssertionFailedException( "Should not reach this state" );
        }
    }
    
    public String runSafe( String s_in ) {
        return runCommand( s_in );
    }
}
