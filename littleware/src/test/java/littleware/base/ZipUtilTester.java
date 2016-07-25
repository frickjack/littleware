package littleware.base;

import com.google.inject.Inject;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;
import static junit.framework.TestCase.assertEquals;
import littleware.base.feedback.Feedback;
import littleware.test.LittleTest;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Zip and unzip some test data
 */
@RunWith(LittleTestRunner.class)
public class ZipUtilTester {

    private static final Logger log = Logger.getLogger(ZipUtilTester.class.getName());
    private final ZipUtil zipUtil;
    private final Feedback feedback;
    private final FileUtil fileUtil;

    @Inject
    public ZipUtilTester(ZipUtil zipUtil, FileUtil fileUtil, Feedback fb) {
        this.zipUtil = zipUtil;
        this.fileUtil = fileUtil;
        this.feedback = fb;
    }

    private final File testDir = new File( Whatever.Folder.Temp.getFolder(), "ZipUtilTester");


    /**
     * Setup a test folder
     */
    @Before
    public void setUp() {
        try {
            fileUtil.buildTestTree( testDir );
        } catch (Exception ex) {
            LittleTest.handle(ex);
        }
    }


    @Test
    public void testZip() {
        try {
            final File testZip = new File("ZipUtilTester.zip");
            if (testZip.exists()) {
                testZip.delete();
            }
            final File info = zipUtil.zip(testDir, testZip, feedback);
            assertEquals("Got expected zip file", info, testZip);
            assertTrue("Zip file is not empty",
                    info.exists() && (info.length() > 0));
            // time to unzip
            final File unzipFolder = new File(Whatever.Folder.Temp.getFolder(), "UnzipTest");
            fileUtil.deleteR( unzipFolder, 20 );
            zipUtil.unzip(info, unzipFolder, feedback);
            final List<File> startList = fileUtil.lsR(testDir);
            final List<File> endList = fileUtil.lsR(unzipFolder);
            int startFileCount = 0;
            int endFileCount = 0;
            int startDirCount = 0;
            int endDirCount = 0;
            for (File scan : startList) {
                if (scan.isFile()) {
                    startFileCount++;
                } else if (scan.isDirectory()) {
                    startDirCount++;
                }
            }
            for (File scan : endList) {
                if (scan.isFile()) {
                    endFileCount++;
                } else if (scan.isDirectory()) {
                    endDirCount++;
                }
            }
            assertTrue("Unzip got correct file count: " + endFileCount + "==" + startFileCount,
                    endFileCount == startFileCount);
            assertTrue("Unzip got correct dir count: " + endDirCount + " vs " + startDirCount,
                    endDirCount == startDirCount + 1 // +1 for parent directory
                    );
        } catch (Exception ex) {
            LittleTest.handle( ex );
        }
    }
}
