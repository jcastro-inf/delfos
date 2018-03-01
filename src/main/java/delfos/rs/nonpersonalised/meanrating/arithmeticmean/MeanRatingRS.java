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
package delfos.rs.nonpersonalised.meanrating.arithmeticmean;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.database.DAOMeanRatingProfile;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Sistema de recomendación que realiza la recomendación basándose en el rating medio de los productos. No se recomienda
 * utilizar este sistema de recomendación en un sistema real como sistema de recomendación principal. Se puede usar para
 * tareas complementarias, como calcular recomendaciones en caso de que el usuario no obtenga recomendaciones con otros
 * sistemas o para deshacer empates entre productos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 *
 * @version 1.0 (03‎ de Jjulio‎ de ‎2012)
 * @version 1.1 (28 de Febrero de 2013)
 * @version 1.2 21-Mar-2013 Implementada la persistencia en base de datos.
 */
public class MeanRatingRS extends CollaborativeRecommender<MeanRatingRSModel> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que sólo llama al constructor de la superclase.
     */
    public MeanRatingRS() {
        super();
    }

    @Override
    public <RatingType extends Rating> MeanRatingRSModel buildRecommendationModel(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadRatingsDataset {

        List<MeanRating> sortedMeanRatings
                = datasetLoader.getContentDataset().parallelStream()
                .map(item -> {

                    double meanRating = datasetLoader.getRatingsDataset()
                            .getItemRatingsRated(item.getId())
                            .values().parallelStream()
                            .mapToDouble(rating -> rating.getRatingValue().doubleValue())
                            .average()
                            .orElse(Double.NaN);

                    return new MeanRating(item, meanRating);
                })
                .collect(Collectors.toList());
        Collections.sort(sortedMeanRatings);
        return new MeanRatingRSModel(sortedMeanRatings);
    }

    @Override
    public <RatingType extends Rating> Collection<Recommendation> recommendToUser(
            DatasetLoader<RatingType> datasetLoader,
            MeanRatingRSModel model,
            long idUser,
            Set<Long> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        Map<Item, MeanRating> meanRatingsByItem = model
                .getSortedMeanRatings().parallelStream()
                .collect(Collectors.toMap(meanRating -> meanRating.getItem(), Function.identity()));

        List<Recommendation> recommendations = candidateItems.parallelStream()
                .map(idItem -> datasetLoader.getContentDataset().get(idItem))
                .map(item -> meanRatingsByItem.containsKey(item) ? meanRatingsByItem.get(item) : new MeanRating(item, Double.NaN))
                .map(meanRating -> new Recommendation(meanRating.getItem(), meanRating.getPreference()))
                .collect(Collectors.toList());

        return recommendations;
    }

    @Override
    public <RatingType extends Rating> MeanRatingRSModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Long> users, Collection<Long> items, DatasetLoader<RatingType> datasetLoader) throws FailureInPersistence {
        DAOMeanRatingProfile dAOMeanRatingProfile = new DAOMeanRatingProfile();
        return dAOMeanRatingProfile.loadModel(databasePersistence, users, items, datasetLoader);
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, MeanRatingRSModel model) throws FailureInPersistence {
        DAOMeanRatingProfile dAOMeanRatingProfile = new DAOMeanRatingProfile();
        dAOMeanRatingProfile.saveModel(databasePersistence, model);
    }
}
