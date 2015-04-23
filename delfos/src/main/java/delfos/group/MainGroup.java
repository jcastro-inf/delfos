package delfos.group;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.casestudy.fromxmlfiles.GroupXMLexperimentsExecution;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.predictionvalidation.CrossFoldPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.NoValidation;
import delfos.group.factories.GroupRatingsFilterFactory;
import delfos.group.factories.GroupRecommenderSystemsFactory;
import delfos.group.grouplevelcasestudy.parallel.GroupLevelCaseStudy_parallel;
import delfos.group.groupsofusers.measuresovergroups.CommonRatings;
import delfos.group.groupsofusers.measuresovergroups.FuzzyCliqueMeasure;
import delfos.group.groupsofusers.measuresovergroups.GroupMeasure;
import delfos.group.groupsofusers.measuresovergroups.MaximumDistanceInGraph;
import delfos.group.groupsofusers.measuresovergroups.NumberOfRatings;
import delfos.group.groupsofusers.measuresovergroups.SizeOfGroup;
import delfos.group.groupsofusers.measuresovergroups.SumDistanceInGraph;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.MAE;
import delfos.rs.trustbased.FixedGraph;
import delfos.rs.trustbased.implicittrustcomputation.ShambourLu_UserBasedImplicitTrustComputation;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal que sirve como punto de entrada para ejecutar la
 * recomendación a grupos implementada hasta el momento. No proporciona interfaz
 * y las opciones son introducidas usando el método <b>Hard coded</b>.
 *
 * @author Jorge Castro Gallardo
 */
public class MainGroup {

    /**
     * Ejecuta la biblioteca como recomendación a grupos. Requiere que ya se
     * haya inicializado la clase {@link ConsoleParameters} y que se haya hecho
     * la llamada al método {@link ConsoleParameterParser#start(java.lang.String[])
     * }para recoger los parámetros de la linea de comandos.
     *
     * @param consoleParameters
     * @return True si se ejecuta algún caso definido, false si no hace nada.
     */
    public static boolean executeConsole_Group(ConsoleParameters consoleParameters) {
        GroupRecommenderSystemsFactory.getInstance().copyInSingleUserRecommender();
        GroupRatingsFilterFactory.getInstance();

        if (consoleParameters.isDefined("-default")) {
            consoleParameters.printUnusedParameters(System.err);
            defaultBehaviour();
            return true;
        }

        if (consoleParameters.isDefined("-executeGroupXML")) {
            try {
                String xmlExperimentsDirectory = consoleParameters.getValue("-executeGroupXML");

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

        if (consoleParameters.isDefined("-testFilter")) {

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
                Logger.getLogger(MainGroup.class.getName()).log(Level.SEVERE, null, ex);
            }

            return true;
        }

        if (consoleParameters.isDefined("-groupLevelCaseStudy")) {

            final int SIZE_OF_GROUPS;
            {
                int num;
                try {
                    num = Integer.parseInt(consoleParameters.getValue("-sizeOfGroups"));
                } catch (UndefinedParameterException ex) {
                    num = 3;
                }
                SIZE_OF_GROUPS = num;
            }

            final int NUM_GROUPS;
            {
                int num;
                try {
                    num = Integer.parseInt(consoleParameters.getValue("-numGroups"));
                } catch (UndefinedParameterException ex) {
                    num = 100;
                }
                NUM_GROUPS = num;
            }

            final long SEED;
            {
                long num;
                try {
                    num = Long.parseLong(consoleParameters.getValue("-seed"));
                } catch (UndefinedParameterException ex) {
                    num = 1;
                }
                SEED = num;
            }

            final String experimentsDirectory;
            {
                String experimentsDirectory_aux;
                try {
                    experimentsDirectory_aux = consoleParameters.getValue("-experimentsDirectory");
                } catch (UndefinedParameterException ex) {
                    experimentsDirectory_aux = "experiments-grs";
                }
                experimentsDirectory = experimentsDirectory_aux;
            }

            consoleParameters.printUnusedParameters(System.err);
            groupLevelCaseStudy(SEED, NUM_GROUPS, SIZE_OF_GROUPS, experimentsDirectory);
            return true;
        }

        if (consoleParameters.isDefined(GroupRecommendationManager.GRS_RECOMMENDATION_PARAMMETER)) {
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

        filterCaseStudy.setDirectory(new File("experiments" + File.separator).getAbsolutePath());
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

    private static void defaultBehaviour() {
        DefaultExecution defaultExecution = new DefaultExecution();
        defaultExecution.execute();
    }

    private static void xmlExperimentsExecution(String experimentsDirectory, String datasetDirectory, int numExecutions, long seed) {
        try {
            GroupXMLexperimentsExecution execution = new GroupXMLexperimentsExecution(
                    experimentsDirectory,
                    datasetDirectory,
                    numExecutions,
                    seed);
            execution.execute();
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }

    private static void printHelp() {
        //TODO: Implementar la ayuda por linea de comandos.
        throw new UnsupportedOperationException("Not implemented the help for this command.");
    }

    private static void groupLevelCaseStudy(long seed, int numGroups, int sizeOfGroups, String experimentsDirectory) {

        try {
            GroupLevelCaseStudy_parallel groupLevelCaseStudy = new GroupLevelCaseStudy_parallel();

            File datasetDirectory = new File(experimentsDirectory + File.separator + "dataset" + File.separator);
            DatasetLoader<? extends Rating> datasetLoader = FilterCaseStudy.getDatasets(datasetDirectory).get(0);

            File grsDirectory = new File(experimentsDirectory + File.separator);
            ArrayList<GroupRecommenderSystem> recommenders = FilterCaseStudy.getRecommenders(grsDirectory);

            ArrayList<GroupMeasure> groupMeasures = new ArrayList<>();

            groupMeasures.add(new FuzzyCliqueMeasure(new FixedGraph("ShambourLu_ImplicitTrustComputation()), 0, 0,25, 0, 0,25.graph", new ShambourLu_UserBasedImplicitTrustComputation()), 0, 0.25, 0, 0.25));
            groupMeasures.add(new FuzzyCliqueMeasure(new FixedGraph("ShambourLu_ImplicitTrustComputation()), 0, 1, 0, 1.graph", new ShambourLu_UserBasedImplicitTrustComputation()), 0, 1, 0, 1));

            groupMeasures.add(new MaximumDistanceInGraph(new ShambourLu_UserBasedImplicitTrustComputation()));
            groupMeasures.add(new SumDistanceInGraph(new ShambourLu_UserBasedImplicitTrustComputation()));

            groupMeasures.add(new NumberOfRatings());
            groupMeasures.add(new CommonRatings(2));
            groupMeasures.add(new CommonRatings(3));
            groupMeasures.add(new CommonRatings(4));
            groupMeasures.add(new SizeOfGroup());
            ArrayList<GroupEvaluationMeasure> evaluationMeasures = new ArrayList<>();
            evaluationMeasures.add(new MAE());

            groupLevelCaseStudy.execute(
                    seed,
                    datasetLoader,
                    new FixedGroupSize_OnlyNGroups(numGroups, sizeOfGroups),
                    recommenders.toArray(new GroupRecommenderSystem[0]),
                    new NoValidation(),
                    new CrossFoldPredictionProtocol(10),
                    groupMeasures.toArray(new GroupMeasure[0]),
                    evaluationMeasures);

        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        }
    }
}
