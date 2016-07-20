package littleware.lgo;

import java.util.Optional;
import java.util.logging.Logger;
import static org.junit.Assert.assertTrue;


/**
 * Verify that the Xml help loader works
 */
public class XmlLgoHelpTester {
    private static final Logger log = Logger.getLogger( XmlLgoHelpTester.class.getName() );

    
    /**
     * Test loading a well known help file
     */
    public void testHelpLoad () {
        final LgoHelpLoader   mgrHelp = new XmlLgoHelpLoader();
        final Optional<LgoHelp> help = mgrHelp.loadHelp( "littleware.lgo.EzHelpCommand" );
        assertTrue( "Able to load EzHelpCommand help info", help.isPresent() );
    }
}
