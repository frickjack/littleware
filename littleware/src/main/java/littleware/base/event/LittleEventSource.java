package littleware.base.event;

/**
 * Source of LittleEvents
 */
public interface LittleEventSource {

    public void addLittleListener(LittleListener listener);

    public void removeLittleListener(LittleListener listener);

}
