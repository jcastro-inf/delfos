package delfos.constants;

import java.io.File;

/**
 * Almacena las constantes de test.
 *
 * @author Jorge
 */
public class TestConstants {

    public static final String databaseTestUserName = "testuser";
    public static final String databaseTestPassword = "testuser";
    public static final String databaseTestDatabaseName = "testdatabase";
    public static final String databaseTestHost = "localhost";
    public static final int databaseTestPort = 8306;
    public static final String databaseTestPrefix = "test_";

    /**
     * Directorio en que se guardan todos los archivos generados para los tests.
     */
    public static final String TEST_DATA_DIRECTORY = "." + File.separator + "test-temp" + File.separator;

    static {
        new File(TEST_DATA_DIRECTORY).mkdir();
    }

    private TestConstants() {
    }
}
