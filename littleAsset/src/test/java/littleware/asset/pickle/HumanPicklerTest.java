package littleware.asset.pickle;

import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.GenericAsset;
import littleware.test.LittleTestRunner;
import org.junit.runner.RunWith;

/**
 * Specialization just injects HumanPickler into PickleTester base class
 */
@RunWith(LittleTestRunner.class)
public class HumanPicklerTest extends PickleTestBase {

    @Inject
    public HumanPicklerTest(HumanPicklerProvider picklerProvider, Provider<GenericAsset.GenericBuilder> genericProvider) {
        super(picklerProvider, genericProvider);
    }
    
}
