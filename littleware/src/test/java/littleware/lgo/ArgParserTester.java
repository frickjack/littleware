package littleware.lgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.lgo.AbstractLgoBuilder;
import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;

/**
 * Test AbstractLgoCommand.parseArg
 */
public class ArgParserTester {
    private static final Logger log = Logger.getLogger(ArgParserTester.class.getName());


    public void testParser() {
        final Map<String, String> mapDefault = new HashMap<>();
        for (int i = 0; i < 10; ++i) {
            mapDefault.put(Integer.toString(i), Integer.toString(i));
        }
        try {
            {
                final Map<String, String> mapResult = AbstractLgoBuilder.processArgs(new ArrayList<>(), mapDefault);
                assertTrue("Got expected empty-args result size: " + mapResult.size(),
                        mapResult.size() == mapDefault.size());
            }
            {
                final Map<String, String> mapResult = AbstractLgoBuilder.processArgs(
                        Arrays.asList("-1", "1000", "-2", "2000", "-3", "3000"),
                        mapDefault);
                assertTrue("Got expected empty-args result size: " + mapResult.size(),
                        mapResult.size() == mapDefault.size());
                for (int i = 1; i < 4; ++i) {
                    assertTrue("Got expected arg value " + i + ": "
                            + mapResult.get(Integer.toString(i)),
                            Integer.toString(1000 * i).equals(mapResult.get(Integer.toString(i))));
                }
            }
            {
                for (int i = 0; i < 10; ++i) {
                    mapDefault.put(Integer.toString(i), null);
                }

                final Map<String, String> mapResult = AbstractLgoBuilder.processArgs(
                        Arrays.asList("-1", "1000", "-2", "2000", "-3", "3000"),
                        mapDefault);
                for (int i = 4; i < 10; ++i) {
                    final String sI = Integer.toString(i);
                    assertTrue("Got expected null arg value " + i + ": " + mapResult.get(sI),
                            null == mapResult.get(sI));
                }

                for (int i = 1; i < 4; ++i) {
                    assertTrue("Got expected arg value " + i + ": "
                            + mapResult.get(Integer.toString(i)),
                            Integer.toString(1000 * i).equals(mapResult.get(Integer.toString(i))));
                }
            }
            {
                // another test
                mapDefault.clear();
                for (String sOpt : new String[]{
                            "path", "diskpath", "pipeline", "facility", "comment", "state"
                        }) {
                    mapDefault.put(sOpt, null);
                }
                final Map<String, String> mapTest = AbstractLgoBuilder.processArgs(
                        Arrays.asList("", "-path", "/KungFu/Episodes/Episode0/", "-state", "Ready", "-facility", "Paprikaas", "-pipeline", "Tx", ""),
                        mapDefault);
                assertTrue("Diskpath unset: " + mapTest.get("diskpath"), null == mapTest.get("diskpath"));
            }
        } catch (Exception ex) {
            LittleTest.handle( ex );
        }
    }
}
