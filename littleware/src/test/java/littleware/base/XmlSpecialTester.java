package littleware.base;

import java.util.logging.Logger;
import java.util.logging.Level;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Run the XmlSpecial encoder/decoder through some paces.
 */
public class XmlSpecialTester {

    private static final Logger log = Logger.getLogger(XmlSpecialTester.class.getName());

    /**
     * Run some generic encode/decode tests
     */
    @Test
    public void testEncodeDecode() {
        final String rawStr;
        final String encodedStr;

        {
            final StringBuilder rawStrBuilder = new StringBuilder();
            final StringBuilder encodeStringBuilder = new StringBuilder();

            for (XmlSpecial n_special : XmlSpecial.values()) {
                String s_decode = XmlSpecial.decode(n_special.getEncoding());
                log.log(Level.INFO, "decode( " + n_special.getEncoding() + ") -> "
                        + s_decode + ", expecting: " + n_special.getChar()
                );
                assertTrue(s_decode.equals(Character.toString(n_special.getChar())));
                assertTrue(n_special.getEncoding().equals(XmlSpecial.encode(s_decode))
                );
                rawStrBuilder.append(n_special.getChar());
                encodeStringBuilder.append(n_special.getEncoding());
            }
            rawStr = rawStrBuilder.toString();
            encodedStr = encodeStringBuilder.toString();
        }
        assertTrue(XmlSpecial.encode(rawStr).equals(encodedStr));
        final String decodeStr = XmlSpecial.decode(encodedStr);
        log.log(Level.INFO, "decode( \"" + encodedStr + "\" ) -> \"" + decodeStr + "\", expected: " + rawStr);
        assertTrue(decodeStr.equals(rawStr));

        // Test with non-special chars
        String s_test_raw = "bla " + rawStr + " bla " + rawStr + "bla ";
        String s_test_encoded = "bla " + encodedStr + " bla " + encodedStr + "bla ";

        assertTrue(XmlSpecial.encode(s_test_raw).equals(s_test_encoded));
        assertTrue(XmlSpecial.decode(s_test_encoded).equals(s_test_raw));
    }

}
