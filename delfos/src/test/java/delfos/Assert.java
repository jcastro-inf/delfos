package delfos;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 12-mar-2014
 */
public class Assert {

    public static final void assertStringArrayEquals(String[] expected, String[] actual) {
        assertEquals("Feature value size does not match", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            String expectedValue = expected[i];
            String actualValue = actual[i];
            if (!expectedValue.equals(actualValue)) {
                fail("expected same: <" + Arrays.asList(expected) + "> was not: <" + Arrays.asList(actual) + ">");
            }
        }
    }
}
