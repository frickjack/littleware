package littleware.asset;

import com.google.common.collect.ImmutableList;

/**
 * Context of a remote method call. Each remote call receives its own context.
 * LittleContext should only be instantiated in remote method marshal code, then
 * passed around through chained calls to local services.
 */
public interface LittleContext {
    public static class LittleEdit {}
    
    /**
     * Token call executing under - later add
     * logic to extract metadata if necessary ...
     */
    String getToken();
    
    /**
     * State is one of:
     *   READING, WRITING, COMMITTING, DONE
     */
    String getState();

    ImmutableList<LittleEdit> commit();    
    public interface ContextFactory {
        LittleContext  build(String token);
    }
    
}
