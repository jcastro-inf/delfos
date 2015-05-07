package delfos.main.managers.recommendation.singleuser.gui.swing;

import delfos.ConsoleParameters;
import delfos.main.managers.CaseUseMode;
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import delfos.view.SwingGUI;

/**
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class BuildConfigurationFileGUI extends CaseUseMode {

    /**
     * Parámetro de la linea de comandos para crear el modelo de un sistema de
     * recomendación mediante interfaz gráfica de usuario en Swing.
     */
    public static final String BUILDX_COMMAND_LINE_PARAMETER = "--single-user-build-x";

    public static BuildConfigurationFileGUI getInstance() {
        return BuildRecommenderSystemConfigurationFileHolder.INSTANCE;
    }

    @Override
    public String getModeParameter() {
        return BUILDX_COMMAND_LINE_PARAMETER;
    }

    private static class BuildRecommenderSystemConfigurationFileHolder {

        public static final BuildConfigurationFileGUI INSTANCE = new BuildConfigurationFileGUI();
    }

    public BuildConfigurationFileGUI() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);

        SwingGUI.initRSBuilderGUI(configurationFile);
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        StringBuilder message = new StringBuilder();

        message.append("\t\t" + BuildConfigurationFileGUI.BUILDX_COMMAND_LINE_PARAMETER);
        message.append(": This option opens a configuration file and allows to edit it using the swing interface..\n");
        message.append("\t\n");

        message.append("Optional arguments");
        message.append("\n" + ArgumentsRecommendation.RECOMMENDER_SYSTEM_CONFIGURATION_FILE);
        message.append(": To specify the configuration file to be opened.");

        return message.toString();
    }

}
