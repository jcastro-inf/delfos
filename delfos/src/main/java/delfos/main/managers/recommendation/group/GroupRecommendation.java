package delfos.main.managers.recommendation.group;

import delfos.ConsoleParameters;
import delfos.common.Global;
import delfos.main.managers.CaseUseModeManager;
import delfos.main.managers.recommendation.ArgumentsRecommendation;

/**
 *
 * @version 22-oct-2014
 * @author Jorge Castro Gallardo
 */
public class GroupRecommendation extends CaseUseModeManager {

    /**
     * Parámetro de la linea de comandos para usar el modo non-personalised.
     */
    public static final String GROUP_MODE = "-group-recommendation";
    /**
     * Parámetro de la linea de comandos para especificar los usuarios del
     * grupo.
     */
    public static final String TARGET_GROUP = "-group-members";

    public static final String BUILD_COMMAND_LINE_PARAMETER = "--build";

    private static final GroupRecommendation instance = new GroupRecommendation();

    public static GroupRecommendation getInstance() {
        return instance;
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        if (Recommend.getInstance().isRightManager(consoleParameters)) {
            Recommend.getInstance().manageCaseUse(consoleParameters);
        } else if (BuildRecommendationModel.getInstance().isRightManager(consoleParameters)) {
            BuildRecommendationModel.getInstance().manageCaseUse(consoleParameters);
        } else {
            Global.show(getUserFriendlyHelpForThisCaseUse());
        }

    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getModeParameter() {
        return GROUP_MODE;
    }

    public String getUserFriendlyHelpForThisCaseUse_unused() {
        StringBuilder str = new StringBuilder();
        str.append("\tRECOMMENDER SYSTEM USAGE\n");
        str.append("\t\t" + GroupRecommendation.BUILD_COMMAND_LINE_PARAMETER
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

}
