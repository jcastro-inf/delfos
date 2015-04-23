package delfos.rs.nonpersonalised;

import java.util.Collection;
import java.util.List;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.GenericRecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;

/**
 * Interfaz para introducir la semántica de un sistema de recomendación no
 * personalizado, es decir, que recomienda siempre los mismos productos
 * independientemente del usuario al que se recomienden.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
