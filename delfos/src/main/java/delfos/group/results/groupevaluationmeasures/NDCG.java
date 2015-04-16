package delfos.group.results.groupevaluationmeasures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.io.xml.evaluationmeasures.NDCGXML;
import static delfos.results.evaluationmeasures.NDCG.computeDCG;
import delfos.rs.recommendation.Recommendation;

/**
 * Evalúa las recomendaciones de un sistema aplicando nDCG, usando logaritmo en
 * base 2. Se calcula el nDCG por usuarios y luego se hace la media. Se aplica
 * la consideración sobre el nDGC hecha por Baltrunas en el paper:
 * <p>
 * <p>
 * Baltrunas L, Makcinskas T, Ricci F (2010) Group recommendations with rank
 * aggregation and collaborative ﬁltering. In: Proceedings of the 4th ACM
 * conference on Recommender Systems, RecSys ’10. ACM, New York, pp 119–126
 *
 * @author Jorge Castro Gallardo
 *
 * @version 20-Noviembre-2013
 */
public class NDCG extends GroupEvaluationMeasure {

    public NDCG() {
        super();
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        List<Double> ndcgByMember = new ArrayList<>();

        for (Map.Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {

            GroupOfUsers groupOfUsers = entry.getKey();
            List<Recommendation> recommendations = entry.getValue();

            if (recommendations.isEmpty()) {
                continue;
            }

            for (int idUser : groupOfUsers) {

                List<Recommendation> idealRecommendations = new ArrayList<>();
                List<Recommendation> recommendationsIntersectUserRatings = new ArrayList<>();
                Map<Integer, ? extends Rating> userRatings;
                try {
                    userRatings = testDataset.getUserRatingsRated(idUser);
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    throw new IllegalArgumentException(ex);
                }

                for (Recommendation recommendation : recommendations) {
                    int idItem = recommendation.getIdItem();
                    if (userRatings.containsKey(idItem)) {
                        idealRecommendations.add(new Recommendation(idItem, userRatings.get(idItem).ratingValue));
                        recommendationsIntersectUserRatings.add(recommendation);
                    }
                }

                if (!idealRecommendations.isEmpty()) {

                    Collections.sort(idealRecommendations);

                    double idealGain = computeDCG(idealRecommendations, userRatings);
                    double gain = computeDCG(recommendationsIntersectUserRatings, userRatings);
                    double score = gain / idealGain;

                    ndcgByMember.add(score);
                }

            }
        }

        Collections.sort(ndcgByMember);

        double min = ndcgByMember.get(0);
        double percentile25 = ndcgByMember.get((int) (ndcgByMember.size() * 0.25));
        double mean = new MeanIterative(ndcgByMember).getMean();
        double percentile75 = ndcgByMember.get((int) (ndcgByMember.size() * 0.75));
        double max = ndcgByMember.get(ndcgByMember.size() - 1);

        return new GroupMeasureResult(this, mean, NDCGXML.getElement(ndcgByMember), ndcgByMember);
    }

    @Override
    public GroupMeasureResult agregateResults(Collection<GroupMeasureResult> results) {

        List<Double> ndcgJoin = new ArrayList<>();

        for (GroupMeasureResult result : results) {
            List<Double> ndcgPerUser = (List<Double>) result.getDetailedResult();
            ndcgJoin.addAll(ndcgPerUser);
        }

        Collections.sort(ndcgJoin);

        return new GroupMeasureResult(this, new MeanIterative(ndcgJoin).getMean(), NDCGXML.getElement(ndcgJoin), ndcgJoin);
    }

}
