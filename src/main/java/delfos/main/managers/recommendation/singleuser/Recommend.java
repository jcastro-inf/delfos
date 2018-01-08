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

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.Global;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.main.managers.CaseUseSubManager;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.RECOMMEND;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.RECOMMEND_SHORT;
import static delfos.main.managers.recommendation.ArgumentsRecommendation.extractConfigurationFile;
import static delfos.main.managers.recommendation.singleuser.SingleUserRecommendation.SINGLE_USER_MODE;
import delfos.rs.RecommenderSystem;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @version 20-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Recommend extends CaseUseSubManager {

    /**
     * Parámetro de la linea de comandos para especificar a qué usuario se desea recomendar.
     */
    public final static String USER_COMMAND_LINE_PARAMETER = "-u";

    public static Recommend getInstance() {
        return Holder.INSTANCE;
    }

    public static void recommendToUser(String configurationFile, long idUser) {

        RecommenderSystemConfiguration rsc
                = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);
        recommendToUser(rsc, idUser);
    }

    private static class Holder {

        private static final Recommend INSTANCE = new Recommend();
    }

    private Recommend() {
        super(SingleUserRecommendation.getInstance());
    }

    public boolean isSingleUserRecommendationCaseUse(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(SINGLE_USER_MODE)
                && (consoleParameters.isFlagDefined(RECOMMEND)
                || consoleParameters.isFlagDefined(RECOMMEND_SHORT));
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return isSingleUserRecommendationCaseUse(consoleParameters);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        manageSingleUserRecommendation(consoleParameters);
    }

    public static void manageSingleUserRecommendation(ConsoleParameters consoleParameters) {

        String configurationFile = extractConfigurationFile(consoleParameters);

        int idUser = extractIdUser(consoleParameters);

        RecommenderSystemConfiguration rsc
                = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

        User user = rsc.datasetLoader.getUsersDataset().getUser(idUser);

        RecommendationsToUser recommendToUser = recommendToUser(rsc, user);

        rsc.recommdendationsOutputMethod.writeRecommendations(recommendToUser);
    }

    public static int extractIdUser(ConsoleParameters consoleParameters) {
        String idUserString;
        try {
            idUserString = consoleParameters.getValue(USER_COMMAND_LINE_PARAMETER);
        } catch (UndefinedParameterException ex) {
            Global.showWarning("Not defined user to recommend, use parameter '" + ex.getParameterMissing() + "'\n");
            Global.showError(ex);
            ERROR_CODES.MANAGE_RATING_DATABASE_USER_NOT_DEFINED.exit(ex);
            idUserString = null;
        }
        int idUser;
        try {
            idUser = Integer.parseInt(idUserString);
        } catch (NumberFormatException ex) {
            Global.showError(ex);
            ERROR_CODES.USER_ID_NOT_RECOGNISED.exit(ex);
            throw ex;
        }
        return idUser;
    }

    public static void recommendToUser(RecommenderSystemConfiguration rsc, long idUser) {
        RecommendationsToUser recommendToUser = recommendToUser(rsc, rsc.datasetLoader.getUsersDataset().getUser(idUser));
        rsc.recommdendationsOutputMethod.writeRecommendations(recommendToUser);
    }

    public static RecommendationsToUser recommendToUser(RecommenderSystemConfiguration rsc, User user) {

        @SuppressWarnings("unchecked")
        RecommenderSystem<Object> recommender = (RecommenderSystem<Object>) rsc.recommenderSystem;

        DatasetLoader<? extends Rating> datasetLoader = rsc.datasetLoader;
        RecommendationsToUser recommendationsToUser;

        Set<Item> candidateItems = rsc.recommendationCandidatesSelector.candidateItems(datasetLoader, user);

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("List of candidate items for user " + user + " size: " + candidateItems.size() + "\n");
            Global.showInfoMessage("\t" + candidateItems + "\n");
        }

        Object recommendationModel;
        try {
            Global.showMessageTimestamped("Computing recommendations");
            recommendationModel = PersistenceMethodStrategy.loadModel(
                    recommender, rsc.persistenceMethod,
                    Arrays.asList(user.getId()),
                    candidateItems.stream().map(item -> item.getId()).collect(Collectors.toSet()),
                    datasetLoader);
            Global.showMessageTimestamped("Computed recommendations");
        } catch (FailureInPersistence ex) {
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
            throw new IllegalArgumentException(ex);
        }

        recommendationsToUser = recommender.recommendToUser(
                datasetLoader,
                recommendationModel,
                user,
                candidateItems
        );

        return recommendationsToUser;

    }

}
