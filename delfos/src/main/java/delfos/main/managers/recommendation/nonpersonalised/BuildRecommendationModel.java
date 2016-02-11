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
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.configfile.rs.single.RecommenderSystemConfiguration;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.main.managers.CaseUseSubManager;
import delfos.main.managers.recommendation.ArgumentsRecommendation;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.PersistenceMethodStrategy;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @version 20-oct-2014
 * @author Jorge Castro Gallardo
 */
public class BuildRecommendationModel extends CaseUseSubManager {

    public static BuildRecommendationModel getInstance() {
        return Holder.INSTANCE;
    }

    private static class Holder {

        private static final BuildRecommendationModel INSTANCE = new BuildRecommendationModel();
    }

    private BuildRecommendationModel() {
        super(NonPersonalisedRecommendation.getInstance());
    }

    @Override
    public boolean isRightManager(ConsoleParameters consoleParameters) {
        return consoleParameters.isFlagDefined(NonPersonalisedRecommendation.NON_PERSONALISED_MODE)
                && consoleParameters.isFlagDefined(ArgumentsRecommendation.BUILD_RECOMMENDATION_MODEL);
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        String configurationFile = ArgumentsRecommendation.extractConfigurationFile(consoleParameters);

        if (!new File(configurationFile).exists()) {
            ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(new FileNotFoundException("Configuration file '" + configurationFile + "' not found"));
        }

        RecommenderSystemConfiguration rsc = RecommenderSystemConfigurationFileParser.loadConfigFile(configurationFile);

        if (rsc.recommenderSystem instanceof NonPersonalisedRecommender) {
            NonPersonalisedRecommender nonPersonalisedRecommender = (NonPersonalisedRecommender) rsc.recommenderSystem;
            Object recomenderSystemModel = buildRecommendationModel(nonPersonalisedRecommender, rsc.datasetLoader);
            saveRecommendationModel(rsc, recomenderSystemModel);
        } else {
            IllegalStateException ise = new IllegalStateException(rsc.recommenderSystem.getAlias() + " is not a non-personalised recommender system (Must implement " + NonPersonalisedRecommender.class);
            ERROR_CODES.NOT_A_RECOMMENDER_SYSTEM.exit(ise);
        }
    }

    private Object buildRecommendationModel(
            NonPersonalisedRecommender<? extends Object> nonPersonalisedRecommender,
            DatasetLoader<? extends Rating> datasetLoader) {

        Object recomenderSystemModel = null;
        try {
            Global.showMessageTimestamped("Building recommendation model.");
            recomenderSystemModel = nonPersonalisedRecommender.buildRecommendationModel(datasetLoader);
            Global.showMessageTimestamped("Built recommendation model.");
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (CannotLoadUsersDataset ex) {
            ERROR_CODES.CANNOT_LOAD_USERS_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        }
        return recomenderSystemModel;
    }

    private void saveRecommendationModel(RecommenderSystemConfiguration rsc, Object recomenderSystemModel) {
        try {
            Global.showMessageTimestamped("Saving recommendation model.");
            PersistenceMethodStrategy.saveModel(rsc.recommenderSystem, rsc.persistenceMethod, recomenderSystemModel);
            Global.showMessageTimestamped("Saved recommendation model.");
        } catch (FailureInPersistence ex) {
            ERROR_CODES.FAILURE_IN_PERSISTENCE.exit(ex);
        }
    }

    public String getSynopsis() {
        return "EMPTY_SYNOPSIS";
    }
}
