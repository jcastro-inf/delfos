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
package delfos.rs.collaborativefiltering.knn.memorybased.nwr;

import delfos.common.Global;
import delfos.common.exceptions.CouldNotPredictRating;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.MatchRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.predictiontechniques.PredictionTechnique;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación basado en el filtrado colaborativo basado en usuarios, también denominado User-User o
 * filtrado colaborativo basado en memoria. Este sistema de recomendación no realiza un cálculo de perfil de usuarios o
 * productos, sino que en el momento de la predicción, calcula los k vecinos más cercanos al usuario activo, es decir,
 * los k ({@link KnnMemoryBasedCFRS#neighborhoodSize}) usuarios más similares
 * ({@link KnnMemoryBasedCFRS#similarityMeasure}). La predicción de la valoración de un producto i para un usuario u se
 * realiza agregando las valoraciones de los vecinos del usuario u sobre el producto i, utilizando para ello una técnica
 * de predicción ({@link KnnMemoryBasedCFRS#predictionTechnique})
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 27-02-2013
 */
public class KnnMemoryBasedNWR extends KnnMemoryBasedCFRS {

    private static final long serialVersionUID = 1L;

    public KnnMemoryBasedNWR() {
        super();
    }

    @Override
    public Collection<Recommendation> recommendWithNeighbors(RatingsDataset<? extends Rating> ratingsDataset, User user, List<Neighbor> neighbors, Collection<Item> candidateItems) {

        int neighborhoodSize = getNeighborhoodSize();
        PredictionTechnique predictionTechnique = (PredictionTechnique) getParameterValue(PREDICTION_TECHNIQUE);

        return KnnMemoryBasedNWR.recommendWithNeighbors(
                ratingsDataset,
                user.getId(),
                neighbors, neighborhoodSize,
                predictionTechnique,
                candidateItems
        );
    }

    public static Collection<Recommendation> recommendWithNeighbors(
            RatingsDataset<? extends Rating> ratingsDataset,
            Integer idUser,
            List<Neighbor> _neighborhood,
            int neighborhoodSize,
            PredictionTechnique predictionTechnique,
            Collection<Item> candidateItems)
            throws UserNotFound {

        List<Neighbor> neighborhood = _neighborhood.stream()
                .filter(neighbor -> !Double.isNaN(neighbor.getSimilarity()))
                .filter(neighbor -> neighbor.getSimilarity() > 0)
                .collect(Collectors.toList());

        neighborhood.sort(Neighbor.BY_SIMILARITY_DESC);

        Collection<Recommendation> recommendations = candidateItems.parallelStream()
                .map(item -> {
                    Collection<MatchRating> match = new ArrayList<>();
                    int numNeighborsUsed = 0;
                    Map<Integer, ? extends Rating> itemRatingsRated;
                    try {
                        itemRatingsRated = ratingsDataset.getItemRatingsRated(item.getId());
                    } catch (ItemNotFound ex) {
                        Global.showError(ex);
                        return new Recommendation(item, Double.NaN);
                    }
                    for (Neighbor neighbor : neighborhood) {

                        Rating rating = itemRatingsRated.get(neighbor.getIdNeighbor());
                        if (rating != null) {
                            match.add(new MatchRating(RecommendationEntity.ITEM, (User) neighbor.getNeighbor(), item, rating.getRatingValue(), neighbor.getSimilarity()));
                            numNeighborsUsed++;
                        }

                        if (numNeighborsUsed >= neighborhoodSize) {
                            break;
                        }
                    }

                    try {
                        double predicted = predictionTechnique.predictRating(idUser, item.getId(), match, ratingsDataset);
                        return new Recommendation(item, predicted);
                    } catch (CouldNotPredictRating ex) {
                        return new Recommendation(item, Double.NaN);
                    }
                })
                .collect(Collectors.toList());

        return recommendations;
    }
}
