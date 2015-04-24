package delfos.configuration;

import delfos.ERROR_CODES;
import delfos.common.Global;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author jcastro
 */
public class ConfigurationManager {

    /**
     * Directorio dentro del que sólo se guardan archivos de configuración.
     */
    public static File CONFIGURATION_DIRECTORY;

    /**
     * Extension associated to the library configurations.
     */
    private static final String CONFIGURATION_FILE_EXTENSION = ".xml";

    static {
        String userHome = System.getenv("HOME");

        if (userHome == null || userHome.isEmpty()) {
            throw new IllegalStateException("Please, set the $HOME environment variable.");
        }

        if (!new File(userHome).exists()) {
            Global.showWarning("The home directory '" + userHome + "' does not exists.");
        }

        CONFIGURATION_DIRECTORY = new File(userHome + File.separator + ".config" + File.separator + ".delfos" + File.separator);
    }

    public static void setConfigurationDirectory(File configurationDirectory) {

        checkConfigurationDirectory(configurationDirectory);

        CONFIGURATION_DIRECTORY = configurationDirectory;

        createConfigurationDirectory();

    }

    public static void checkConfigurationDirectory(File configurationDirectory) {

        if (!configurationDirectory.exists()) {
            Global.showMessage("The configuration directory '" + configurationDirectory + "' does not exists. Creating.");
        } else if (!configurationDirectory.isDirectory()) {
            throw new IllegalArgumentException("The file '" + configurationDirectory + "' is not a directory, cannot be the configuration directory");
        }
    }

    public static void createConfigurationDirectory() {
        if (!CONFIGURATION_DIRECTORY.exists()) {
            boolean mkdirs = ConfigurationManager.CONFIGURATION_DIRECTORY.mkdirs();
            if (!mkdirs) {
                IOException ex = new IOException("Cannot create '" + ConfigurationManager.CONFIGURATION_DIRECTORY.getAbsolutePath() + "' directory");
                ERROR_CODES.CANNOT_WRITE_LIBRARY_CONFIG_FILE.exit(ex);
            }
        }
    }

    /**
     * Returns the configuration file associated to the specified scope of the
     * library. Examples of scopes: path, swing-gui, configured-datasets.
     *
     * If the configuration file does not exists, it is created.
     *
     * @param configurationScope Scope to retrieve its configuration file.
     * @return File pointing to a xml file that has the configuration.
     */
    public static File getConfigurationFile(Configuration configurationScope) {

        File configurationFile = new File(CONFIGURATION_DIRECTORY.getAbsolutePath()
                + File.separator + configurationScope.getScopeName() + CONFIGURATION_FILE_EXTENSION);

        return configurationFile;
    }

}
