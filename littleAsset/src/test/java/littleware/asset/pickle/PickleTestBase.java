package littleware.asset.pickle;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.io.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.Asset;
import littleware.asset.GenericAsset;
import littleware.asset.GenericAsset.GenericBuilder;


import littleware.base.*;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Test PickleMakers
 */
public abstract class PickleTestBase {
    private static final Logger log = Logger.getLogger(PickleTestBase.class.getName());

    
    private final Provider<? extends PickleMaker<Asset>> picklerProvider;
    private final Provider<GenericBuilder> assetProvider;

    /**
     * Constructor stashes PickleType to test against
     */
    @Inject
    public PickleTestBase(Provider<? extends PickleMaker<Asset>> providePickler,
            Provider<GenericAsset.GenericBuilder> assetProvider
            ) {
        this.picklerProvider = providePickler;
        this.assetProvider = assetProvider;
    }


    /**
     * Stupid little test - check whether pickling an GenericAsset.GENERIC
     * asset twice yields the same result both times.
     * Not all PickleMaker may require that.
     */
    @Test
    public void testPickleTwice() {
        try {
            final Asset testAsset = assetProvider.get().
                    name("bogus_pickletest_asset").
                    homeId(UUID.randomUUID()).
                    parentId(UUID.randomUUID()).
                    comment("Test comment").
                    creatorId(UUID.randomUUID()).
                    lastUpdaterId(UUID.randomUUID()).
                    lastUpdate("Bla bla").build();

            final StringWriter stringWriter = new StringWriter();
            picklerProvider.get().pickle(testAsset, stringWriter);
            final String firstPickle = stringWriter.toString();
            log.log(Level.INFO, "First pickle of test asset got: " + firstPickle);

            stringWriter.getBuffer().setLength(0);
            picklerProvider.get().pickle(testAsset, stringWriter);
            final String secondPickle = stringWriter.toString();
            assertTrue("Pickle twice, got same result", firstPickle.equals(secondPickle));
        } catch (Exception ex) {
            log.log(Level.INFO, "Caught unexected: " + ex + ", " +
                    BaseException.getStackTrace(ex));
            fail("Caught unexpected: " + ex);
        }
    }
}

