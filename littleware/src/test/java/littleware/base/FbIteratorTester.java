package littleware.base;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.FeedbackIterableBuilder;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Run FeedbackIterator through a simple exercise
 */
@RunWith(LittleTestRunner.class)
public class FbIteratorTester {
    private static final Logger log = Logger.getLogger( FbIteratorTester.class.getName() );

    private final FeedbackIterableBuilder fbItBuilder;
    private final Feedback defaultFb;

    @Inject
    public FbIteratorTester( FeedbackIterableBuilder fbItBuilder, Feedback defaultFb ) {
        this.fbItBuilder = fbItBuilder;
        this.defaultFb = defaultFb;
    }

    @Test
    public void testFbIterator() {
        final List<Integer> testList = new ArrayList<Integer>();

        for ( int i=0; i < 10; ++ i ) {
            testList.add( i );
        }
        int progress = 0;
        for( Integer scan : fbItBuilder.build(testList, defaultFb) ) {
            log.log( Level.INFO, "Scanning: " + scan );
            assertTrue( "Progress advancing: " + progress + " less than " + defaultFb.getProgress(),
                    progress < defaultFb.getProgress()
                    );
            progress = defaultFb.getProgress();
        }
    }
}
