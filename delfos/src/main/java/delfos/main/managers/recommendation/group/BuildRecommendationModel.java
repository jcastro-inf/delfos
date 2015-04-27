package delfos.main.managers.recommendation.group;

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
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.view.SwingGUI;

/**
 *
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
        return consoleParameters.isDefined(ArgumentsGroupRecommendation.GROUP_MODE)
                && consoleParameters.isDefined(ArgumentsGroupRecommendation.BUILD_COMMAND_LINE_PARAMETER);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        manageBuildRecommendationModel(consoleParameters);
    }

    public static void manageBuildRecommendationModel(ConsoleParameters consoleParameters) {
        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);

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
        str.append("\t\t" + ArgumentsGroupRecommendation.BUILD_COMMAND_LINE_PARAMETER
                + ": This option is used to build the "
                + "model using a CONFIGFILE defined by parameter "
                + ArgumentsRecommendation.RECOMMENDER_SYSTEM_CONFIGURATION_FILE
                + ". If the config file is not "
                + "specified, search config.xml in the actual directory."
                + "If the file doesn't exists, shows a GUI to select the "
                + "recommender system options and create the recommender"
                + "system config file with the options you need.\n");
        str.append("\t\n");

        str.append("\t" + ArgumentsRecommendation.RECOMMENDER_SYSTEM_CONFIGURATION_FILE
                + " [CONFIGFILE]: The option " + ArgumentsRecommendation.RECOMMENDER_SYSTEM_CONFIGURATION_FILE
                + " indicates to use a previously built recommender system "
                + "specified in the CONFIGFILE\n");
        str.append("\t\n");

        return str.toString();
    }

    public String getSynopsis() {
        return "EMPTY_SYNOPSIS";
    }
}
