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
package delfos.experiment.casestudy.parallel;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.RecommenderSystem;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Set;
import java.util.function.Function;

/**
 * Realiza la ejecución de una recomendación de un usuario y la almacena. Al
 * terminar la tarea, libera los recursos que ya no son necesarios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class SingleUserRecommendationTaskExecutor implements Function<SingleUserRecommendationTask, RecommendationsToUser> {

    @Override
    public RecommendationsToUser apply(SingleUserRecommendationTask task) {
        RecommenderSystem recommenderSystem = task.getRecommenderSystem();
        DatasetLoader<? extends Rating> datasetLoader = task.getDatasetLoader();
        User user = datasetLoader.getUsersDataset().get(task.getIdUser());
        Set<Item> candidateItems = task.getCandidateItems();
        Object model = task.getRecommendationModel();
        try {
            Recommendations recommendationsGeneric = recommenderSystem.recommendToUser(
                    datasetLoader,
                    model,
                    user,
                    candidateItems);

            RecommendationsToUser recommendations = new RecommendationsToUser(
                    user,
                    recommendationsGeneric.getRecommendations(),
                    recommendationsGeneric.getRecommendationComputationDetails());
            return recommendations;
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
            throw new IllegalStateException(ex);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            throw new IllegalStateException(ex);
        }

    }
}
