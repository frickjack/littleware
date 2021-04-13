package littleware.lgo;

import java.util.List;
import littleware.base.feedback.Feedback;

/**
 * Interface for command objects that can be 
 * executed via the LittleGo app-launcher or servlet.
 */
public interface LgoCommand {

    /**
     * Getter for globally unique command name property.
     * Use normal reverse-DNS technique to
     * give each command subtype a unique name.
     * 
     * @return the full-name string
     */
    public String getName();

    public String runCommandLine( Feedback feedback ) throws Exception;
    public Object runCommand( Feedback feedback ) throws Exception;

    public interface LgoBuilder {
        public String getName();
        public LgoCommand buildFromArgs(List<String> args);
        public LgoCommand buildWithInput( Object input );
    }

}
