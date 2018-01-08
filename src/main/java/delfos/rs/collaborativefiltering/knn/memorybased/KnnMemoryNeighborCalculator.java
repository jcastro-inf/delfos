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
package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

public final class KnnMemoryNeighborCalculator implements Function<KnnMemoryNeighborTask, Neighbor> {

    public KnnMemoryNeighborCalculator() {
        super();
    }

    @Override
    public Neighbor apply(KnnMemoryNeighborTask task) {

        KnnCollaborativeRecommender rs = task.rs;
        DatasetLoader<? extends Rating> datasetLoader = task.datasetLoader;

        User user = task.user;
        User neighbor = task.neighbor;

        if (user.equals(neighbor)) {
            return new Neighbor(RecommendationEntity.USER, neighbor, Double.NaN);
        }
        CollaborativeSimilarityMeasure similarityMeasure_ = (CollaborativeSimilarityMeasure) rs.getParameterValue(KnnCollaborativeRecommender.SIMILARITY_MEASURE);

        boolean defaultRating_ = (Boolean) rs.getParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING);

        boolean inverseFrequency_ = (Boolean) rs.getParameterValue(KnnCollaborativeRecommender.INVERSE_FREQUENCY);
        double caseAmp = ((Number) rs.getParameterValue(KnnCollaborativeRecommender.CASE_AMPLIFICATION)).doubleValue();
        boolean relevanceFactor_ = (Boolean) rs.getParameterValue(KnnCollaborativeRecommender.RELEVANCE_FACTOR);
        int relevanceFactorValue_ = (Integer) rs.getParameterValue(KnnCollaborativeRecommender.RELEVANCE_FACTOR_VALUE);

        Map<Long, ? extends Rating> activeUserRated = datasetLoader.getRatingsDataset()
                .getUserRatingsRated(user.getId());
        Map<Long, ? extends Rating> neighborRatings = datasetLoader.getRatingsDataset()
                .getUserRatingsRated(neighbor.getId());

        Set<Long> intersectionSet = new TreeSet<>(activeUserRated.keySet());
        intersectionSet.retainAll(neighborRatings.keySet());

        Collection<CommonRating> common;

        if (!defaultRating_) {
            common = CommonRating.intersection(datasetLoader, user, neighbor);
        } else {
            common = getCommonRatingUnion(datasetLoader, rs, user, activeUserRated, neighbor, neighborRatings);
        }

        if (inverseFrequency_) {
            int numAllUsers = datasetLoader.getRatingsDataset().allUsers().size();
            for (CommonRating c : common) {
                try {
                    double numUserRatedThisItem = datasetLoader.getRatingsDataset().sizeOfItemRatings(c.getIdCommon());
                    double inverseFrequencyValue = numAllUsers / numUserRatedThisItem;
                    inverseFrequencyValue = (double) Math.log(inverseFrequencyValue);
                    c.setWeight(inverseFrequencyValue);
                } catch (ItemNotFound ex) {
                    throw new IllegalArgumentException("Cant find product '" + c.getIdCommon());
                }
            }
        }

        double sim;
        try {
            sim = similarityMeasure_.similarity(common, datasetLoader.getRatingsDataset());

            if (sim > 0) {
                if (relevanceFactor_) {

                    if (intersectionSet.size() < relevanceFactorValue_) {
                        sim = sim * ((double) intersectionSet.size() / relevanceFactorValue_);
                    }
                }
                sim = (double) Math.pow(sim, caseAmp);
            }
        } catch (CouldNotComputeSimilarity ex) {
            sim = Double.NaN;
        }
        return new Neighbor(RecommendationEntity.USER, neighbor, sim);
    }

    private Collection<CommonRating> getCommonRatingUnion(DatasetLoader<? extends Rating> datasetLoader, KnnCollaborativeRecommender rs, User user, Map<Long, ? extends Rating> activeUserRated, User neighbor, Map<Long, ? extends Rating> neighborRatings) {

        byte defaultRatingValue = ((Integer) rs.getParameterValue(KnnCollaborativeRecommender.DEFAULT_RATING_VALUE)).byteValue();

        Set<Long> union = new TreeSet<>(activeUserRated.keySet());

        union.addAll(neighborRatings.keySet());
        if (union.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        Collection<CommonRating> common = new ArrayList<>();
        for (long idItem : union) {
            Rating r1 = activeUserRated.get(idItem);
            Rating r2 = neighborRatings.get(idItem);

            double d1;
            if (r1 == null) {
                d1 = defaultRatingValue;
            } else {
                d1 = r1.getRatingValue().doubleValue();
            }

            double d2;
            if (r2 == null) {
                d2 = defaultRatingValue;
            } else {
                d2 = r2.getRatingValue().doubleValue();
            }
            common.add(new CommonRating(
                    RecommendationEntity.ITEM,
                    idItem,
                    RecommendationEntity.USER,
                    user.getId(), neighbor.getId(),
                    d1, d2));
        }

        return common;
    }

}
