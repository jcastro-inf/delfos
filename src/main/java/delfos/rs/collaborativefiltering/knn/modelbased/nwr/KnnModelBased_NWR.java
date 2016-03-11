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
package delfos.rs.collaborativefiltering.knn.modelbased.nwr;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRS;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelBasedCFRSModel;
import delfos.rs.collaborativefiltering.knn.modelbased.KnnModelItemProfile;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sistema de recomendación basado en el filtrado colaborativo basado en
 * productos, también denominado Item-Item o filtrado colaborativo basado en
 * modelo. Este sistema de recomendación calcula un perfil de cada producto. El
 * perfil consta de los k vecinos más cercanos, es decir, los k
 * ({@link KnnModelBasedCFRS#NEIGHBORHOOD_SIZE}) productos más similares
 * ({@link KnnModelBasedCFRS#SIMILARITY_MEASURE}). La predicción de la
 * valoración de un producto i para un usuario u se realiza agregando las
 * valoraciones del usuario u sobre los productos vecinos del producto i,
 * utilizando para ello una técnica de predicción
 * ({@link KnnModelBasedCFRS#PREDICTION_TECHNIQUE})
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnModelBased_NWR
        extends KnnModelBasedCFRS {

    private static final long serialVersionUID = 1L;

    @Override
    public Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> datasetLoader, KnnModelBasedCFRSModel model, Integer idUser, java.util.Set<Integer> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, ItemNotFound {

        PredictionTechnique prediction = (PredictionTechnique) getParameterValue(KnnModelBasedCFRS.PREDICTION_TECHNIQUE);

        Collection<Recommendation> recommendationList = new LinkedList<>();
        Map<Integer, ? extends Rating> userRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
        if (userRated.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        int neighborhoodSize = (Integer) getParameterValue(NEIGHBORHOOD_SIZE);

        int itemsWithProfile = 0;
        for (int idItem : candidateItems) {
            List<MatchRating> matchRatings = new LinkedList<>();
            KnnModelItemProfile profile = model.getItemProfile(idItem);

            if (profile == null) {
                continue;
            }

            itemsWithProfile++;

            for (Neighbor neighbor : profile.getAllNeighbors()) {
                int idItemNeighbor = neighbor.getIdNeighbor();
                double similarity = neighbor.getSimilarity();
                Rating rating = userRated.get(idItemNeighbor);
                if (rating != null) {
                    matchRatings.add(new MatchRating(RecommendationEntity.USER, idUser, idItemNeighbor, rating, similarity));
                    if (Global.isVerboseAnnoying()) {
                        Global.showInfoMessage(idItemNeighbor + "\the prediction is\t" + rating + "\n");
                    }
                }

                if (matchRatings.size() >= neighborhoodSize) {
                    break;
                }
            }

            if (!matchRatings.isEmpty()) {
                Double predictedRating;
                try {
                    predictedRating = prediction.predictRating(idUser, idItem, matchRatings, datasetLoader.getRatingsDataset());
                    recommendationList.add(new Recommendation(idItem, predictedRating));
                } catch (UserNotFound | ItemNotFound ex) {
                    throw new IllegalArgumentException(ex);
                } catch (CouldNotPredictRating ex) {
                }
            }

        }
        return recommendationList;
    }
}
