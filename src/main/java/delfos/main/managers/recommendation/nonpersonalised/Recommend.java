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
import delfos.rs.recommendation.RecommendationsToUser;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 *
 * @version 22-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class Recommend extends CaseUseSubManager {

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
            user = new User(new Long(idUser));
        } else {
            user = User.ANONYMOUS_USER;
        }

        RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

        RecommendationsToUser recommendations = computeRecommendations(rsc, user);

        rsc.recommdendationsOutputMethod.writeRecommendations(recommendations);

    }

    public static RecommendationsToUser computeRecommendations(RecommenderSystemConfiguration rsc, User user) throws ItemNotFound, CannotLoadContentDataset, RuntimeException, CannotLoadRatingsDataset {
        if (!(rsc.recommenderSystem instanceof NonPersonalisedRecommender)) {
            IllegalStateException ise = new IllegalStateException(rsc.recommenderSystem.getAlias() + " is not a non-personalised recommender system (Must implement " + NonPersonalisedRecommender.class);
            ERROR_CODES.NOT_A_RECOMMENDER_SYSTEM.exit(ise);
            throw ise;
        }
        NonPersonalisedRecommender nonPersonalisedRecommender = (NonPersonalisedRecommender) rsc.recommenderSystem;
        Object recommendationModel;
        try {
            recommendationModel = PersistenceMethodStrategy.loadModel(rsc);
        } catch (FailureInPersistence ex) {
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
            throw new IllegalStateException(ex);
        }
        Collection<Long> candidateItems;
        try {
            candidateItems = rsc.recommendationCandidatesSelector.candidateItems(rsc.datasetLoader, user).stream().map(item -> item.getId()).collect(Collectors.toSet());
        } catch (UserNotFound ex) {
            if (rsc.datasetLoader instanceof ContentDatasetLoader) {
                ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) rsc.datasetLoader;
                candidateItems = contentDatasetLoader.getContentDataset().allIDs();
            } else {
                candidateItems = rsc.datasetLoader.getRatingsDataset().allRatedItems();
            }
        }
        Collection<Recommendation> recommendOnly = nonPersonalisedRecommender.recommendOnly(rsc.datasetLoader, recommendationModel, candidateItems);
        return new RecommendationsToUser(User.ANONYMOUS_USER, recommendOnly);
    }
}
