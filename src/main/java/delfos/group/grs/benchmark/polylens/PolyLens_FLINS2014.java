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
package delfos.group.grs.benchmark.polylens;

import delfos.common.aggregationoperators.Mean;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystemAdapter;
import delfos.group.grs.SingleRecommendationModel;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.explanation.GroupModelWithExplanation;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.util.Set;

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
 * Published in: Proceeding ECSCW'01 Proceedings of the seventh conference on European Conference on Computer Supported
 * Cooperative Work Pages, 199 - 218.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 20-May-2013
 */
public class PolyLens_FLINS2014 extends GroupRecommenderSystemAdapter<SingleRecommendationModel, GroupModelPseudoUser> {

    private final AggregationOfIndividualRatings aggregationOfIndividualRatings;

    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta para la predicción de la valoración. Si no
     * se modifica, su valor por defecto es 20
     */
    public static final Parameter NEIGHBORHOOD_SIZE = new Parameter("Neighborhood_size", new IntegerParameter(1, 9999, 60));

    public PolyLens_FLINS2014() {
        final KnnMemoryBasedCFRS knnMemory = new KnnMemoryBasedCFRS();

        knnMemory.setParameterValue(KnnCollaborativeRecommender.SIMILARITY_MEASURE, new PearsonCorrelationCoefficient());
        knnMemory.setParameterValue(KnnCollaborativeRecommender.RELEVANCE_FACTOR, 20);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING_VALUE, null);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING, false);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.CASE_AMPLIFICATION, 1);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.NEIGHBORHOOD_SIZE, 60);
        knnMemory.setParameterValue(KnnCollaborativeRecommender.PREDICTION_TECHNIQUE, new WeightedSum());

        aggregationOfIndividualRatings = new AggregationOfIndividualRatings(knnMemory, new Mean());
        addParameter(NEIGHBORHOOD_SIZE);

        addParammeterListener(() -> {
            knnMemory.setParameterValue(KnnMemoryBasedCFRS.NEIGHBORHOOD_SIZE, getParameterValue(NEIGHBORHOOD_SIZE));
        });
    }

    public PolyLens_FLINS2014(int neighborhoodSize) {
        this();

        setParameterValue(PolyLens_FLINS2014.NEIGHBORHOOD_SIZE, neighborhoodSize);
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public <RatingType extends Rating> SingleRecommendationModel buildRecommendationModel(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return aggregationOfIndividualRatings.buildRecommendationModel(datasetLoader);
    }

    @Override
    public <RatingType extends Rating> GroupModelPseudoUser buildGroupModel(
            DatasetLoader<RatingType> datasetLoader,
            SingleRecommendationModel RecommendationModel,
            GroupOfUsers groupOfUsers)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return aggregationOfIndividualRatings.buildGroupModel(datasetLoader, RecommendationModel, groupOfUsers).getGroupModel();
    }

    @Override
    public <RatingType extends Rating> GroupRecommendations recommendOnly(
            DatasetLoader<RatingType> datasetLoader, SingleRecommendationModel recommendationModel, GroupModelPseudoUser groupModel, GroupOfUsers groupOfUsers, Set<Item> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        return aggregationOfIndividualRatings.recommendOnly(
                datasetLoader,
                recommendationModel,
                new GroupModelWithExplanation<>(groupModel, "No Explanation Provided"),
                groupOfUsers,
                candidateItems);
    }

}
