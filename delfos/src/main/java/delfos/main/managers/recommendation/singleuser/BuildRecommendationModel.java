package delfos.main.managers.recommendation.singleuser;

import java.io.File;
import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.main.managers.CaseUseManager;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.BUILD_RECOMMENDATION_MODEL;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.BUILD_RECOMMENDATION_MODEL_SHORT;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.RECOMMENDER_SYSTEM_CONFIGURATION_FILE;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.extractConfigurationFile;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.view.SwingGUI;

/**
 *
 * @version 20-oct-2014
 * @author Jorge Castro Gallardo
 */
public class BuildRecommendationModel implements CaseUseManager {

    public static BuildRecommendationModel getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final BuildRecommendationModel INSTANCE = new BuildRecommendationModel();
    }

    private BuildRecommendationModel() {
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        if (consoleParameters.isDefined(ArgumentsSingleUserRecommendation.SINGLE_USER_MODE)) {
            return consoleParameters.isDefined(BUILD_RECOMMENDATION_MODEL_SHORT) || consoleParameters.isDefined(BUILD_RECOMMENDATION_MODEL);
        } else {
            return false;
        }
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        manageBuildRecommendationModel(consoleParameters);
    }

    public static void manageBuildRecommendationModel(ConsoleParameters consoleParameters) {
        String configurationFile = extractConfigurationFile(consoleParameters);

        if (new File(configurationFile).exists()) {
            buildRecommendationModel(configurationFile);
        } else {
            SwingGUI.initRSBuilderGUI(configurationFile);
        }
    }

    public static void buildRecommendationModel(String configurationFile) {
        try {
            RecommenderSystemConfiguration rsc
                    = RecommenderSystemConfigurationFileParser
                    .loadConfigFile(configurationFile);

            @SuppressWarnings("unchecked")
            GenericRecommenderSystem<Object> recommender
                    = (GenericRecommenderSystem<Object>) rsc.recommenderSystem;

            Object recomenderSystemModel;
            Global.showMessageTimestamped("Building recommendation model.");
            recomenderSystemModel = recommender.buildRecommendationModel(rsc.datasetLoader);
            Global.showMessageTimestamped("Built recommendation model.");

            PersistenceMethodStrategy.saveModel(
                    rsc.recommenderSystem,
                    rsc.persistenceMethod,
                    recomenderSystemModel);

        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
        } catch (FailureInPersistence ex) {
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
        }
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        StringBuilder str = new StringBuilder();
        str.append("\tRECOMMENDER SYSTEM USAGE\n");
        str.append("\t\t" + BUILD_RECOMMENDATION_MODEL
                + ": This option is used to build the "
                + "model using a CONFIGFILE defined by parameter "
                + RECOMMENDER_SYSTEM_CONFIGURATION_FILE
                + ". If the config file is not "
                + "specified, search config.xml in the actual directory."
                + "If the file doesn't exists, shows a GUI to select the "
                + "recommender system options and create the recommender"
                + "system config file with the options you need.\n");
        str.append("\t\n");

        str.append("\t" + RECOMMENDER_SYSTEM_CONFIGURATION_FILE
                + " [CONFIGFILE]: The option " + RECOMMENDER_SYSTEM_CONFIGURATION_FILE
                + " indicates to use a previously built recommender system "
                + "specified in the CONFIGFILE\n");
        str.append("\t\n");

        return str.toString();
    }

    public String getSynopsis() {
        return "EMPTY_SYNOPSIS";
    }
}
