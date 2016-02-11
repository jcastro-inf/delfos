/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
