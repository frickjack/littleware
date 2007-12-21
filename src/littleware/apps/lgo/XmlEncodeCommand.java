/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package littleware.apps.lgo;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import littleware.base.XmlSpecial;

/**
 * XML encode/decode LgoCommand
 */
public class XmlEncodeCommand extends AbstractLgoCommand {
    private static final Logger olog = Logger.getLogger( XmlEncodeCommand.class.getName () );
    
    /**
     * Constructor just sets the command-name to this.class.getName,
     * and the command-help property to the supplied help object.
     */
    public XmlEncodeCommand() {
        super( XmlEncodeCommand.class.getName() );        
    }
    
    @Override
    public void runCommand() throws LgoException {
        String[] v_argv = this.getCommandArgs();
        
        ResourceBundle bundle_support = ResourceBundle.getBundle( XmlEncodeCommand.class.getName() + "Resources" );


        // create the command line options that we are looking for
        final LongOpt[] v_longopts = {
            new LongOpt(bundle_support.getString("version.option"), LongOpt.NO_ARGUMENT, null, 1),
            new LongOpt(bundle_support.getString("encode.option"), LongOpt.NO_ARGUMENT, null, 'e'),
            new LongOpt(bundle_support.getString("decode.option"), LongOpt.NO_ARGUMENT, null, 'd')
        };

        final Getopt opts = new Getopt(bundle_support.getString("appname"), v_argv, "ed", v_longopts);

        boolean b_encode = true;

        for (int i_opt = opts.getopt();
                i_opt != -1;
                i_opt = opts.getopt()) {
            switch (i_opt) {
                case 1:
                     {
                        System.out.println("littleware.base.XmlSecial version 0.1");
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
                BufferedReader read_stdin = new BufferedReader(new InputStreamReader(System.in));
                for (String s_data = read_stdin.readLine();
                        s_data != null;
                        s_data = read_stdin.readLine()) {
                    if (b_encode) {
                        System.out.println( XmlSpecial.encode(s_data));
                    } else {
                        System.out.println( XmlSpecial.decode(s_data));
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
        System.exit(0);
    }            

}
