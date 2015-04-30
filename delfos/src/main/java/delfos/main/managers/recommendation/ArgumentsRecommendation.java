package delfos.main.managers.recommendation;

import java.io.File;
import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;

/**
 * Clase para establecer los parámetros de la línea de comandos que son comunes
 * a todos los casos de uso relacionados con la petición de recomendaciones.
 *
 * @author Jorge Castro Gallardo
 */
public class ArgumentsRecommendation {

    public static final String BUILD_RECOMMENDATION_MODEL = "--build";
    public static final String BUILD_RECOMMENDATION_MODEL_SHORT = "--b";

    public static final String RECOMMEND = "--recommend";
    public static final String RECOMMEND_SHORT = "--r";

    public static final String RECOMMENDER_SYSTEM_CONFIGURATION_FILE = "-config-file";

    public static final String DEFAULT_RECOMMENDER_SYSTEM_CONFIGURATION_FILE = "recommenderSystemConfiguration.xml";

    public static String extractConfigurationFile(ConsoleParameters consoleParameters) {
        String configurationFile;
        try {
            configurationFile = consoleParameters.getValue(RECOMMENDER_SYSTEM_CONFIGURATION_FILE);
        } catch (UndefinedParameterException ex) {
            Global.showWarning("Configuration file not specified, using default (to specify use " + RECOMMENDER_SYSTEM_CONFIGURATION_FILE + " parameter)\n");
            configurationFile = DEFAULT_RECOMMENDER_SYSTEM_CONFIGURATION_FILE;
        }

        Global.showInfoMessage("Using configuration file '" + new File(configurationFile).getAbsolutePath() + "'");

        if (!new File(configurationFile).exists()) {
            Global.showWarning("Configuration file not found: '" + configurationFile + "'\n");
            ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(new IllegalArgumentException("Configuration file '" + configurationFile + "' not found"));
        }
        return configurationFile;
    }
}
