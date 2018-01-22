package delfos.results.evaluationmeasures.ndcg;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NDCGImplicit extends EvaluationMeasure{

    private static final long serialVersionUID = 1L;

    protected final int listSize;

    public NDCGImplicit() {
        this.listSize = -1;
    }

    protected NDCGImplicit(int listSize) {
        this.listSize = listSize;
    }

    @Override
    public MeasureResult getMeasureResult(
            RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        List<Double> ndcgPerUser = new ArrayList<>();

        for (long idUser : testDataset.allUsers()) {
            try {

                List<Recommendation> recommendations = recommendationResults.getRecommendationsForUser(idUser);
                if (recommendations.isEmpty()) {
                    continue;
                }

                Map<Long, Rating> userRatings = (Map<Long, Rating>) testDataset.getUserRatingsRated(idUser);


                double ndcg = computeNDCG(recommendations, userRatings);

                ndcgPerUser.add(ndcg);

            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        final double ndcg = (double) new MeanIterative(ndcgPerUser).getMean();

        return new MeasureResult(this, ndcg);
    }

    public static <RatingType extends Rating > double computeNDCG(List<Recommendation> recommendations, Map<Long,RatingType> userRatings){
        return computeNDCG(recommendations,userRatings,-1);
    }

    public static <RatingType extends Rating >  double computeNDCG(List<Recommendation> recommendations, Map<Long, RatingType> userRatings, int listSize) {

        Stream<Recommendation> idealRecommendationsStream = userRatings
                .values().parallelStream()
                .map(rating -> new Recommendation(rating.getItem(), rating.getRatingValue()))
                .sorted(Recommendation.BY_PREFERENCE_DESC);

        Stream<Recommendation> recommendationsSortedStream = recommendations.stream()
                        .sorted(Recommendation.BY_PREFERENCE_DESC);

        if (listSize > 0) {
            recommendationsSortedStream = recommendationsSortedStream
                    .limit(listSize);

            idealRecommendationsStream = idealRecommendationsStream
                    .limit(listSize);
        }

        List<Recommendation> idealRecommendations = idealRecommendationsStream.collect(Collectors.toList());
        List<Recommendation> recommendationsSorted = recommendationsSortedStream.collect(Collectors.toList());

        double idealGain = computeDCG(idealRecommendations, userRatings);
        double gain = computeDCG(recommendationsSorted, userRatings);

        final double ndcg;
        if(idealGain == 0){
            ndcg = 0;
        }else {
            ndcg = gain / idealGain;
        }

        if (Double.isNaN(ndcg)) {
            throw new IllegalStateException("NDCG is NaN, possibly because there are ratings with a Zero value");
        }

        return ndcg;
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    /**
     * Computes the DCG of a list of items with respect to a value vector.
     *
     * It considers the rating set as implicit ratings. Therefore, if a recommendation is in the rating set, then it is positive.
     * When a recommendation is not in the rating set, it is considered negative. The ratingValue is considered in the measure.
     *
     * If there are items in the ratings that do not appear in the recommendation list, they are ignored.
     *
     * @param recommendations Recommendations, must be sorted by preference in descending order.
     * @param userRatings
     * @return
     */
    public static <RatingType extends Rating >  double computeDCG(
            List<Recommendation> recommendations,
            Map<Long, RatingType> userRatings) {

        double gain = 0;
        int rank = 1;

        Iterator<Recommendation> iit = recommendations.iterator();
        while (iit.hasNext()) {
            final Recommendation recommendation = iit.next();
            final long idItem = recommendation.getItem().getId();
            final double rating;

            if(userRatings.containsKey(idItem)){
                rating = userRatings.get(idItem).getRatingValue().doubleValue();
            }else{
                rating = 0.0;
            }

            double discount;

            if (rank < base) {
                discount = 1;
            } else {
                discount = 1 / log2(rank);
            }

            double increment = rating * discount;

            gain += increment;
            rank++;
        }

        return gain;
    }

    private static final int base = 2;
    private static final double logBaseChange = Math.log(base);

    public static double log2(int rank) {
        double log2OfRank = Math.log(rank) / logBaseChange;
        return log2OfRank;
    }
}
