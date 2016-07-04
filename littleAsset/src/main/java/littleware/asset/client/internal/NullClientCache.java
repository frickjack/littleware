package littleware.asset.client.internal;

import littleware.asset.client.spi.ClientCache;
import java.util.UUID;
import littleware.asset.Asset;

/**
 * NOOP ClientCache
 */
public class NullClientCache implements ClientCache, java.io.Serializable {
    private static final long serialVersionUID = 42234L;

    public NullClientCache( ) {
    }

    @Override
    public long getTimestamp() {
        return 0L;
    }


    @Override
    public void put(Asset asset) {
    }

    @Override
    public Asset get(UUID uId) {
        return null;
    }

    @Override
    public void put(String key, Asset asset) {
    }

    @Override
    public Asset get(String key) {
        return null;
    }

}
