package delfos.constants;

import delfos.Constants;
import java.io.File;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Clase de la que deben heredar todos los test de la biblioteca.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 26-Septiembre-2013
 */
public class DelfosTest {

    @BeforeClass
    public static void beforeClassTests() {
        Constants.setJUnitTestMode();
    }

    public File getTemporalDirectoryForTest() {
        return new File(TestConstants.TEST_DATA_DIRECTORY + this.getClass().getSimpleName() + File.separator);
    }

    public static File getTemporalDirectoryForTest(Class<? extends DelfosTest> testClass) {
        return new File(TestConstants.TEST_DATA_DIRECTORY + testClass.getSimpleName() + File.separator);
    }

    @Before
    public void beforeAllTests() {
        Constants.setJUnitTestMode();
    }
}
