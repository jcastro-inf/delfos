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
package delfos.group;

import delfos.ConsoleParameters;
import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.group.casestudy.fromxmlfiles.GroupXMLexperimentsExecution;
import delfos.group.factories.GroupRatingsFilterFactory;
import delfos.group.factories.GroupRecommenderSystemsFactory;
import delfos.main.managers.experiment.ExecuteGroupXML;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Clase principal que sirve como punto de entrada para ejecutar la recomendación a grupos implementada hasta elnModel, RatingType>
 * momento. No proporciona interfaz y las opciones son introducidas usando el método <b>Hard coded</b>.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class MainGroup {

    /**
     * Ejecuta la biblioteca como recomendación a grupos. Requiere que ya se haya inicializado la clase
     * {@link ConsoleParameters} y que se haya hecho la llamada al método {@link ConsoleParameterParser#start(java.lang.String[])
     * }para recoger los parámetros de la linea de comandos.
     *
     * @param consoleParameters
     * @return True si se ejecuta algún caso definido, false si no hace nada.
     */
    public static boolean executeConsole_Group(ConsoleParameters consoleParameters) {
        GroupRecommenderSystemsFactory.getInstance().copyInSingleUserRecommender();
        GroupRatingsFilterFactory.getInstance();

        if (consoleParameters.isParameterDefined(ExecuteGroupXML.MODE_PARAMETER)) {
            try {
                String xmlExperimentsDirectory = consoleParameters.getValue(ExecuteGroupXML.MODE_PARAMETER);

                final int NUM_EJECUCIONES;
                {
                    int num;
                    try {
                        num = Integer.parseInt(consoleParameters.getValue("-numExec"));
                    } catch (UndefinedParameterException ex) {
                        num = 1;
                    }
                    NUM_EJECUCIONES = num;
                }

                long SEED;
                {
                    long num;
                    try {
                        num = Long.parseLong(consoleParameters.getValue("-seed"));
                    } catch (UndefinedParameterException ex) {
                        num = System.currentTimeMillis();
                    }
                    SEED = num;
                }

                consoleParameters.printUnusedParameters(System.err);
                xmlExperimentsExecution(xmlExperimentsDirectory, xmlExperimentsDirectory + File.separator + "dataset" + File.separator, NUM_EJECUCIONES, SEED);
            } catch (UndefinedParameterException ex) {
                consoleParameters.printUnusedParameters(System.err);
            }

            return true;
        }

        if (consoleParameters.isParameterDefined("-testFilter")) {

            final int NUM_EJECUCIONES, NUM_GROUPS, SIZE_OF_GROUPS;
            final long SEED;
            {
                int num;
                try {
                    num = Integer.parseInt(consoleParameters.getValue("-numExec"));
                } catch (UndefinedParameterException ex) {
                    num = 1;
                }
                NUM_EJECUCIONES = num;
            }

            {
                int num;
                try {
                    num = Integer.parseInt(consoleParameters.getValue("-numGroups"));
                } catch (UndefinedParameterException ex) {
                    num = 659;
                }
                NUM_GROUPS = num;
            }

            {
                int num;
                try {
                    num = Integer.parseInt(consoleParameters.getValue("-sizeOfGroups"));
                } catch (UndefinedParameterException ex) {
                    num = 3;
                }
                SIZE_OF_GROUPS = num;
            }

            {
                long seed;
                try {
                    seed = Long.parseLong(consoleParameters.getValue("-seed"));
                } catch (UndefinedParameterException ex) {
                    seed = System.currentTimeMillis();
                }
                SEED = seed;
            }

            consoleParameters.printUnusedParameters(System.err);
            try {
                filterMethodCaseStudy(NUM_EJECUCIONES, NUM_GROUPS, SIZE_OF_GROUPS, SEED);
            } catch (NumberFormatException | IOException ex) {
                ERROR_CODES.COMMAND_LINE_PARAMETERS_ERROR.exit(ex);
            }

            return true;
        }

        if (consoleParameters.isParameterDefined(GroupRecommendationManager.GRS_RECOMMENDATION_PARAMMETER)) {
            try {
                return GroupRecommendationManager.execute(consoleParameters);
            } catch (Exception ex) {
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
                return true;
            }
        }

        return false;
    }

    private static void filterMethodCaseStudy(
            final int NUM_EJECUCIONES,
            final int NUM_GROUPS,
            final int SIZE_OF_GROUPS,
            final long SEED)
            throws NumberFormatException, IOException {

        FilterCaseStudy filterCaseStudy = new FilterCaseStudy(
                NUM_EJECUCIONES,
                NUM_GROUPS,
                SIZE_OF_GROUPS,
                SEED);

        filterCaseStudy.setDirectory(new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + "experiments" + File.separator).getAbsolutePath());
        try {
            filterCaseStudy.execute();
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }

    private static void xmlExperimentsExecution(String experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
        try {
            GroupXMLexperimentsExecution execution = new GroupXMLexperimentsExecution(
                    experimentsDirectory,
                    datasetDirectory,
                    Optional.of(numExecutions),
                    Optional.of(seed));
            execution.execute();
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }
}
