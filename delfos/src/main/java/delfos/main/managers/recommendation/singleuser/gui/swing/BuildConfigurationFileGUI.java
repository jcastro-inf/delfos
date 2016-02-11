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
