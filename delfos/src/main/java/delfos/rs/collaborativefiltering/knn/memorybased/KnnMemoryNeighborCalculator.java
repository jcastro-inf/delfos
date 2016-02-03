package delfos.rs.collaborativefiltering.knn.memorybased;

import delfos.ERROR_CODES;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.user.User;
import delfos.rs.collaborativefiltering.knn.CommonRating;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.similaritymeasures.CollaborativeSimilarityMeasure;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class KnnMemoryNeighborCalculator implements Function<KnnMemoryNeighborTask, Neighbor> {

    public KnnMemoryNeighborCalculator() {
        super();
    }

    @Override
    public Neighbor apply(KnnMemoryNeighborTask task) {
        User user = task.user;
        User neighbor = task.neighborUser;

        KnnMemoryBasedCFRS rs = task.rs;
        RatingsDataset<? extends Rating> ratingsDataset = task.datasetLoader.getRatingsDataset();

        if (user.equals(neighbor)) {
            return new Neighbor(RecommendationEntity.USER, neighbor, Double.NaN);
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

        final Map<Integer, ? extends Rating> userRatings = ratingsDataset.getUserRatingsRated(user.getId());
        final Map<Integer, ? extends Rating> neighborRatings = ratingsDataset.getUserRatingsRated(neighbor.getId());

        Set<Integer> intersectionSet = userRatings.keySet().stream()
                .filter(idItem -> neighborRatings.containsKey(idItem))
                .collect(Collectors.toSet());

        List<CommonRating> ratingsForSimilarity = intersectionSet.parallelStream()
                .sorted((i1, i2) -> Integer.compare(i1, i2))
                .map(idItem -> {
                    Rating userRating = userRatings.get(idItem);
                    Rating neighborRating = neighborRatings.get(idItem);

                    CommonRating commonRating = new CommonRating(
                            RecommendationEntity.ITEM, idItem,
                            RecommendationEntity.USER, user.getId(), neighbor.getId(),
                            userRating.getRatingValue().floatValue(), neighborRating.getRatingValue().floatValue());
                    return commonRating;
                }).collect(Collectors.toList());

        if (defaultRating_) {
            Set<Integer> onlyOneRated = new TreeSet<>();

            onlyOneRated.addAll(userRatings.keySet());
            onlyOneRated.addAll(neighborRatings.keySet());

            onlyOneRated.removeAll(intersectionSet);

            Set<Integer> union = new TreeSet<>(userRatings.keySet());
            union.addAll(neighborRatings.keySet());
            if (union.isEmpty()) {
                return new Neighbor(RecommendationEntity.USER, neighbor, Double.NaN);
            }
            for (int idItem : onlyOneRated) {
                float userRating = userRatings.containsKey(idItem)
                        ? userRatings.get(idItem).getRatingValue().floatValue()
                        : defaultRatingValue_;

                float neighborRating = neighborRatings.containsKey(idItem)
                        ? neighborRatings.get(idItem).getRatingValue().floatValue()
                        : defaultRatingValue_;
                CommonRating commonRatingCompletedWithDefault = new CommonRating(
                        RecommendationEntity.ITEM, idItem,
                        RecommendationEntity.USER, user.getId(), neighbor.getId(),
                        userRating, neighborRating);
                ratingsForSimilarity.add(commonRatingCompletedWithDefault);
            }
        }

        if (inverseFrequency_) {
            int numAllUsers = ratingsDataset.allUsers().size();
            for (CommonRating commonRating : ratingsForSimilarity) {
                try {
                    float numUserRatedThisItem = ratingsDataset.sizeOfItemRatings(commonRating.getIdCommon());
                    float inverseFrequencyValue = numAllUsers / numUserRatedThisItem;
                    inverseFrequencyValue = (float) Math.log(inverseFrequencyValue);
                    commonRating.setWeight(inverseFrequencyValue);
                } catch (ItemNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                }
            }
        }

        float sim;
        try {
            sim = similarityMeasure_.similarity(ratingsForSimilarity, ratingsDataset);

            if (sim > 0) {
                if (relevanceFactor_) {
                    if (intersectionSet.size() < relevanceFactorValue_) {
                        sim = sim * ((float) intersectionSet.size() / relevanceFactorValue_);
                    }
                }
                sim = (float) Math.pow(sim, caseAmp);
            }
        } catch (CouldNotComputeSimilarity ex) {
            sim = Float.NaN;
        }
        return new Neighbor(RecommendationEntity.USER, neighbor, sim);
    }
}
