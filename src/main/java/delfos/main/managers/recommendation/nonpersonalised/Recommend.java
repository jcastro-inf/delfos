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
package delfos.main.managers.recommendation.nonpersonalised;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.user.User;
import delfos.main.managers.CaseUseSubManager;
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import delfos.main.managers.recommendation.singleuser.SingleUserRecommendation;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.SingleUserRecommendations;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;

/**
 *
 * @version 22-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
class Recommend extends CaseUseSubManager {

    public static Recommend getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final Recommend INSTANCE = new Recommend();
    }

    private Recommend() {
        super(NonPersonalisedRecommendation.getInstance());
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(NonPersonalisedRecommendation.NON_PERSONALISED_MODE)
                && consoleParameters.isFlagDefined(ArgumentsRecommendation.RECOMMEND);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {

        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);
        if (!new File(configurationFile).exists()) {
            ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(new FileNotFoundException("Configuration file '" + configurationFile + "' not found"));
        }
        User user;
        if (consoleParameters.isParameterDefined(SingleUserRecommendation.TARGET_USER)) {
            String idUser = consoleParameters.getValue(SingleUserRecommendation.TARGET_USER);
            user = new User(Integer.parseInt(idUser));
        } else {
            user = User.ANONYMOUS_USER;
        }

        RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

        if (rsc.recommenderSystem instanceof NonPersonalisedRecommender) {
            Chronometer chronometer = new Chronometer();
            NonPersonalisedRecommender nonPersonalisedRecommender = (NonPersonalisedRecommender) rsc.recommenderSystem;
            Object recommendationModel;
            try {
                recommendationModel = PersistenceMethodStrategy.loadModel(rsc);
            } catch (FailureInPersistence ex) {
                ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
                throw new IllegalStateException(ex);
            }

            Collection<Integer> candidateItems;
            try {
                candidateItems = rsc.recommendationCandidatesSelector.candidateItems(rsc.datasetLoader, user);
            } catch (UserNotFound ex) {
                if (rsc.datasetLoader instanceof ContentDatasetLoader) {
                    ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) rsc.datasetLoader;
                    candidateItems = contentDatasetLoader.getContentDataset().allIDs();
                } else {
                    candidateItems = rsc.datasetLoader.getRatingsDataset().allRatedItems();
                }
            }

            try {
                Collection<Recommendation> recommendOnly = nonPersonalisedRecommender.recommendOnly(rsc.datasetLoader, recommendationModel, candidateItems);

                long timeTaken = chronometer.getTotalElapsed();
                rsc.recommdendationsOutputMethod.writeRecommendations(new SingleUserRecommendations(user, recommendOnly, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken)));
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            } catch (CannotLoadRatingsDataset ex) {
                ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            } catch (CannotLoadContentDataset ex) {
                ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            }
        } else {
            IllegalStateException ise = new IllegalStateException(rsc.recommenderSystem.getAlias() + " is not a non-personalised recommender system (Must implement " + NonPersonalisedRecommender.class);
            ERROR_CODES.NOT_A_RECOMMENDER_SYSTEM.exit(ise);
        }
    }
}
