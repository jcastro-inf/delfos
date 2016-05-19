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
package delfos.rs.nonpersonalised;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.GenericRecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 * Interfaz para introducir la semántica de un sistema de recomendación no personalizado, es decir, que recomienda
 * siempre los mismos productos independientemente del usuario al que se recomienden.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 07-ene-2014
 * @param <RecommendationModel>
 */
public abstract class NonPersonalisedRecommender<RecommendationModel> extends GenericRecommenderSystemAdapter<RecommendationModel> {

    public final Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, Integer idUser, Collection<Integer> candidateItems) throws ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return recommendOnly(datasetLoader, model, candidateItems);
    }

    public abstract Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, Collection<Integer> candidateItems) throws ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset;

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.NON_PERSONALISED_RECOMMENDER_SYSTEM;
    }

}
