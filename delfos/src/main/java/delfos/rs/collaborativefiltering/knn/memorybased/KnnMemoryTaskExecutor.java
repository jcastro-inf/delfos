package delfos.rs.collaborativefiltering.knn.memorybased;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.ERROR_CODES;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.SingleTaskExecute;

public final class KnnMemoryTaskExecutor implements SingleTaskExecute<KnnMemoryTask> {

    public KnnMemoryTaskExecutor() {
        super();
    }

    @Override
    public void executeSingleTask(KnnMemoryTask task) {
        int idUser = task.idUser;
        int idNeighbor = task.idNeighbor;
        KnnMemoryBasedCFRS rs = task.rs;
        RatingsDataset<? extends Rating> ratingsDataset = task.ratingsDataset;

        if (idUser == idNeighbor) {
            return;
        }
        CollaborativeSimilarityMeasure similarityMeasure_ = (CollaborativeSimilarityMeasure) rs.getParameterValue(KnnMemoryBasedCFRS.SIMILARITY_MEASURE);

        Byte defaultRatingValue_ = 0;
        boolean defaultRating_ = (Boolean) rs.getParameterValue(KnnMemoryBasedCFRS.DEFAULT_RATING);
        if (defaultRating_) {
            defaultRatingValue_ = ((Integer) rs.getParameterValue(KnnMemoryBasedCFRS.DEFAULT_RATING_VALUE)).byteValue();
        }

        boolean inverseFrequency_ = (Boolean) rs.getParameterValue(KnnMemoryBasedCFRS.INVERSE_FREQUENCY);
        float caseAmp = ((Number) rs.getParameterValue(KnnMemoryBasedCFRS.CASE_AMPLIFICATION)).floatValue();
        boolean relevanceFactor_ = (Boolean) rs.getParameterValue(KnnMemoryBasedCFRS.RELEVANCE_FACTOR);
        int relevanceFactorValue_ = (Integer) rs.getParameterValue(KnnMemoryBasedCFRS.RELEVANCE_FACTOR_VALUE);

        Map<Integer, ? extends Rating> activeUserRated;
        Map<Integer, ? extends Rating> neighborRatings;
        try {
            activeUserRated = ratingsDataset.getUserRatingsRated(idUser);
            neighborRatings = ratingsDataset.getUserRatingsRated(idNeighbor);
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
            activeUserRated = new TreeMap<Integer, Rating>();
            neighborRatings = new TreeMap<Integer, Rating>();
        }

        Set<Integer> intersectionSet = null;

        if (relevanceFactor_) {
            intersectionSet = new TreeSet<Integer>(activeUserRated.keySet());
            intersectionSet.retainAll(neighborRatings.keySet());
        }

        ArrayList<CommonRating> common = new ArrayList<CommonRating>();

        if (!defaultRating_) {

            Set<Integer> intersection;
            if (intersectionSet == null) {
                intersection = new TreeSet<Integer>();
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

                float d1 = r1.ratingValue.floatValue();
                float d2 = r2.ratingValue.floatValue();
                common.add(new CommonRating(RecommendationEntity.ITEM, idItem, RecommendationEntity.USER, idUser, idNeighbor, d1, d2));
            }
        } else {
            Set<Integer> union = new TreeSet<Integer>(activeUserRated.keySet());
            union.addAll(neighborRatings.keySet());
            if (union.isEmpty()) {
                return;
            }
            for (int idItem : union) {
                Rating r1 = activeUserRated.get(idItem);
                Rating r2 = neighborRatings.get(idItem);

                float d1;
                if (r1 == null) {
                    d1 = defaultRatingValue_;
                } else {
                    d1 = r1.ratingValue.floatValue();
                }

                float d2;
                if (r2 == null) {
                    d2 = defaultRatingValue_;
                } else {
                    d2 = r2.ratingValue.floatValue();
                }
                common.add(new CommonRating(RecommendationEntity.ITEM, idItem, RecommendationEntity.USER, idUser, idNeighbor, d1, d2));
            }
        }

        if (inverseFrequency_) {
            int numAllUsers = ratingsDataset.allUsers().size();
            for (CommonRating c : common) {
                try {
                    float numUserRatedThisItem = ratingsDataset.sizeOfItemRatings(c.getIdCommon());
                    float inverseFrequencyValue = numAllUsers / numUserRatedThisItem;
                    inverseFrequencyValue = (float) Math.log(inverseFrequencyValue);
                    c.setWeight(inverseFrequencyValue);
                } catch (ItemNotFound ex) {
                    throw new IllegalArgumentException("Cant find product '" + c.getIdCommon());
                }
            }
        }

        float sim;
        try {
            sim = similarityMeasure_.similarity(common, ratingsDataset);

            if (sim > 0) {//Global.showMessage(numVecinosProbados+"   de "+getRatingsDataset().allUsers().size()+" en "+chronometer.printPartialElapsed());
                if (relevanceFactor_ && intersectionSet.size() < relevanceFactorValue_) {
                    sim = sim * ((float) intersectionSet.size() / relevanceFactorValue_);
                }

                if (caseAmp >= 0) {
                    sim = (float) Math.pow(sim, caseAmp);
                } else {
                    sim = (float) -Math.pow(-sim, caseAmp);
                }

                Neighbor neighbor = new Neighbor(RecommendationEntity.USER, idNeighbor, sim);
                task.setNeighbor(neighbor);
            }
        } catch (CouldNotComputeSimilarity ex) {
        }
    }
}
