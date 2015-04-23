package delfos.group.grs.benchmark.polylens;

import java.util.Collection;
import java.util.List;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterListener;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;

/**
 * Sistema que propone el paper
 *
 * <p>
 * <p>
 * PolyLens: A Recommender System for Groups of Users
 *
 * <p>
 * Mark O'Connor, Dan Cosley, Joseph A. Konstan and John Riedl
 *
 * <p>
 * Published in: Proceeding ECSCW'01 Proceedings of the seventh conference on
 * European Conference on Computer Supported Cooperative Work Pages, 199 - 218.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 20-May-2013
 */
public class PolyLens extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupOfUsers> {

    private final AggregationOfIndividualRecommendations aggregationOfIndividualRecommendations;

    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta
     * para la predicción de la valoración. Si no se modifica, su valor por
     * defecto es 20
     */
    public static final Parameter neighborhoodSize = new Parameter("Neighborhood_size", new IntegerParameter(1, 9999, 60));

    public PolyLens() {
        final KnnMemoryBasedNWR knnMemory = new KnnMemoryBasedNWR(new PearsonCorrelationCoefficient(), 20, null, false, 1, 60, new WeightedSum());
        aggregationOfIndividualRecommendations = new AggregationOfIndividualRecommendations(knnMemory, new MinimumValue());
        addParameter(neighborhoodSize);
        addParammeterListener(new ParameterListener() {
            @Override
            public void parameterChanged() {
                knnMemory.setParameterValue(KnnMemoryBasedNWR.NEIGHBORHOOD_SIZE, getParameterValue(neighborhoodSize));
            }
        });
    }

    public PolyLens(int neighborhoodSize) {
        this();

        setParameterValue(PolyLens.neighborhoodSize, neighborhoodSize);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public SingleRecommendationModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRecommendations.build(datasetLoader);
    }

    @Override
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRecommendations.buildGroupModel(datasetLoader, RecommendationModel, groupOfUsers);
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, SingleRecommendationModel RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRecommendations.recommendOnly(datasetLoader, RecommendationModel, groupModel, groupOfUsers, idItemList);
    }

}
