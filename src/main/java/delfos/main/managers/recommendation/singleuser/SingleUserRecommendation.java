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
package delfos.main.managers.recommendation.singleuser;

import delfos.main.managers.CaseUseModeWithSubManagers;
import delfos.main.managers.CaseUseSubManager;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.BUILD_RECOMMENDATION_MODEL;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.RECOMMENDER_SYSTEM_CONFIGURATION_FILE;
import static delfos.main.managers.recommendation.singleuser.Recommend.USER_COMMAND_LINE_PARAMETER;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @version 22-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class SingleUserRecommendation extends CaseUseModeWithSubManagers {

    private static final SingleUserRecommendation instance = new SingleUserRecommendation();

    public static SingleUserRecommendation getInstance() {
        return instance;
    }

    /**
     * Parámetro de la linea de comandos para usar el modo non-personalised.
     */
    public static final String SINGLE_USER_MODE = "--single-user";
    /**
     * Parámetro de la linea de comandos para especificar a qué usuario se desea
     * recomendar.
     */
    public static final String TARGET_USER = "-u";

    public static void recommendToUser(String configFile, long idUser) {
        Recommend.recommendToUser(configFile, idUser);
    }

    public static void buildRecommendationModel(String configFile) {
        BuildRecommendationModel.buildRecommendationModel(configFile);
    }

    @Override
    public String getUserFriendlyHelpForThisCaseUse() {
        return SINGLE_USER_MODE;
    }

    @Override
    public Collection<CaseUseSubManager> getAllCaseUseSubManagers() {
        ArrayList<CaseUseSubManager> allCaseUseModeSubManagers = new ArrayList<>();

        allCaseUseModeSubManagers.add(BuildRecommendationModel.getInstance());
        allCaseUseModeSubManagers.add(Recommend.getInstance());

        return allCaseUseModeSubManagers;
    }

    @Override
    public String getModeParameter() {
        return SINGLE_USER_MODE;
    }

    private String getUserFriendlyHelpForThisCaseUse_RecommendMode() {
        StringBuilder ret = new StringBuilder();

        ret.append("MANDATORY ARGUMENTS\n");
        ret.append("\t" + USER_COMMAND_LINE_PARAMETER + " ID_USER: Used in ");
        ret.append(SINGLE_USER_MODE).append(" ");
        ret.append("mode to indicate the target user. The recommender system ");
        ret.append("specified in the CONFIGFILE is used to return a list of ");
        ret.append("the most relevant items for the active user (the user with ");
        ret.append("id=ID_USER) \n");

        ret.append("OPTIONAL ARGUMENTS\n");
        ret.append("\t " + RECOMMENDER_SYSTEM_CONFIGURATION_FILE);

        return ret.toString();
    }

    private String getUserFriendlyHelpForThisCaseUse_buildMode() {
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

}
