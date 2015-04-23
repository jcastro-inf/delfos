package delfos.configuration;

import java.io.File;

/**
 *
 * @author jcastro
 */
public class ConfigurationManager {

    /**
     * Directorio dentro del que sólo se guardan archivos de configuración.
     */
    public static File CONFIGURATION_DIRECTORY = new File("~" + File.separator + ".config" + File.separator + ".delfos" + File.separator);
    /**
     * Nombre del archivo donde se guardan los valores de configuración de las
     * rutas por defecto de la biblioteca de recomendación.
     */
    public static final File CONFIGURATION_FILE_NAME = new File(ConfigurationManager.CONFIGURATION_DIRECTORY.getPath() + File.separator + "path.config");

    /**
     * Returns the configuration file associated to the specified scope of the
     * library. Examples of scopes: path, swing-gui, configured-datasets.
     *
     * If the configuration file does not exists, it is created.
     *
     * @param scope
     * @return
     */
    public File getConfigurationFile(String scope) {

    }
}
