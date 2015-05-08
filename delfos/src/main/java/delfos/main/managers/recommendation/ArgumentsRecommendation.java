package delfos.main.managers.recommendation;

import delfos.ConsoleParameters;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import java.io.File;

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

    public static final String RECOMMENDER_SYSTEM_CONFIGURATION_FILE = "-rs-config";

    public static final String DEFAULT_RECOMMENDER_SYSTEM_CONFIGURATION_FILE = "rs-config.xml";

    public static String extractConfigurationFile(ConsoleParameters consoleParameters) {
        String configurationFile;
        try {
            configurationFile = consoleParameters.getValue(RECOMMENDER_SYSTEM_CONFIGURATION_FILE);
        } catch (UndefinedParameterException ex) {
            Global.showInfoMessage("Configuration file not specified, using default (to specify use " + RECOMMENDER_SYSTEM_CONFIGURATION_FILE + " parameter)\n");
            configurationFile = DEFAULT_RECOMMENDER_SYSTEM_CONFIGURATION_FILE;
        }

        Global.showInfoMessage("Using configuration file '" + new File(configurationFile).getAbsolutePath() + "'\n");

        return configurationFile;
    }
}
