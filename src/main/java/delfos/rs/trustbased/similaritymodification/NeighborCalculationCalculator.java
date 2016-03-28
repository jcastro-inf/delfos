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
package delfos.rs.trustbased.similaritymodification;

import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.CASE_AMPLIFICATION;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.DEFAULT_RATING;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.DEFAULT_RATING_VALUE;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.INVERSE_FREQUENCY;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.RELEVANCE_FACTOR;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.RELEVANCE_FACTOR_VALUE;
import static delfos.rs.collaborativefiltering.knn.KnnCollaborativeRecommender.SIMILARITY_MEASURE;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public final class NeighborCalculationCalculator implements SingleTaskExecute<NeighborCalculationTask> {

    public NeighborCalculationCalculator() {
        super();
    }

    @Override
    public void executeSingleTask(NeighborCalculationTask task) {
        int idUser = task.idUser;
        int idNeighbor = task.idNeighbor;
        TrustModificationKnnMemory rs = task.rs;
        RatingsDataset<? extends Rating> ratingsDataset = task.ratingsDataset;

        if (idUser == idNeighbor) {
            return;
        }
        CollaborativeSimilarityMeasure similarityMeasure_ = (CollaborativeSimilarityMeasure) rs.getParameterValue(SIMILARITY_MEASURE);

        Byte defaultRatingValue_ = 0;
        boolean defaultRating_ = (Boolean) rs.getParameterValue(DEFAULT_RATING);
        if (defaultRating_) {
            defaultRatingValue_ = ((Integer) rs.getParameterValue(DEFAULT_RATING_VALUE)).byteValue();
        }

        boolean inverseFrequency_ = (Boolean) rs.getParameterValue(INVERSE_FREQUENCY);
        double caseAmp = ((Number) rs.getParameterValue(CASE_AMPLIFICATION)).doubleValue();
        boolean relevanceFactor_ = (Boolean) rs.getParameterValue(RELEVANCE_FACTOR);
        int relevanceFactorValue_ = (Integer) rs.getParameterValue(RELEVANCE_FACTOR_VALUE);

        Map<Integer, ? extends Rating> activeUserRated;
        Map<Integer, ? extends Rating> neighborRatings;
        try {
            activeUserRated = ratingsDataset.getUserRatingsRated(idUser);
        } catch (UserNotFound ex) {
            activeUserRated = new TreeMap<>();
        }
        try {
            neighborRatings = ratingsDataset.getUserRatingsRated(idNeighbor);
        } catch (UserNotFound ex) {
            neighborRatings = new TreeMap<>();
        }

        Set<Integer> intersectionSet = null;

        if (relevanceFactor_) {
            intersectionSet = new TreeSet<>(activeUserRated.keySet());
            intersectionSet.retainAll(neighborRatings.keySet());
        }

        ArrayList<CommonRating> common = new ArrayList<>();

        if (!defaultRating_) {

            Set<Integer> intersection;
            if (intersectionSet == null) {
                intersection = new TreeSet<>();
                for (int id : activeUserRated.keySet()) {
                    if (neighborRatings.containsKey(id)) {
                        intersection.add(id);
                    }
                }
            } else {
                intersection = intersectionSet;
            }
            if (intersection.isEmpty()) {
                return;
            }
            for (int idItem : intersection) {
                Rating r1 = activeUserRated.get(idItem);
                Rating r2 = neighborRatings.get(idItem);

                double d1 = r1.getRatingValue().doubleValue();
                double d2 = r2.getRatingValue().doubleValue();
                common.add(new CommonRating(RecommendationEntity.ITEM, idItem, RecommendationEntity.USER, idUser, idNeighbor, d1, d2));
            }
        } else {
            Set<Integer> union = new TreeSet<>(activeUserRated.keySet());
            union.addAll(neighborRatings.keySet());
            if (union.isEmpty()) {
                return;
            }
            for (int idItem : union) {
                Rating r1 = activeUserRated.get(idItem);
                Rating r2 = neighborRatings.get(idItem);

                double d1;
                if (r1 == null) {
                    d1 = defaultRatingValue_;
                } else {
                    d1 = r1.getRatingValue().doubleValue();
                }

                double d2;
                if (r2 == null) {
                    d2 = defaultRatingValue_;
                } else {
                    d2 = r2.getRatingValue().doubleValue();
                }
                common.add(new CommonRating(RecommendationEntity.ITEM, idItem, RecommendationEntity.USER, idUser, idNeighbor, d1, d2));
            }
        }

        if (inverseFrequency_) {
            int numAllUsers = ratingsDataset.allUsers().size();
            for (CommonRating c : common) {
                try {
                    double numUserRatedThisItem = ratingsDataset.sizeOfItemRatings(c.getIdCommon());
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
            sim = similarityMeasure_.similarity(common, ratingsDataset);

            if (sim > 0) {
                //Global.showMessage(numVecinosProbados+"   de "+getRatingsDataset().allUsers().size()+" en "+chronometer.printPartialElapsed());
                if (relevanceFactor_ && intersectionSet.size() < relevanceFactorValue_) {
                    sim = sim * ((double) intersectionSet.size() / relevanceFactorValue_);
                }

                if (caseAmp >= 0) {
                    sim = (double) Math.pow(sim, caseAmp);
                } else {
                    sim = (double) -Math.pow(-sim, caseAmp);
                }

                if (Double.isNaN(sim) || Double.isInfinite(sim)) {
                    throw new IllegalArgumentException("Similarity NaN or Infinity.");
                }
                Neighbor neighbor = new Neighbor(RecommendationEntity.USER, idNeighbor, sim);
                task.setNeighbor(neighbor);
            }
        } catch (CouldNotComputeSimilarity ex) {
        }
    }
}