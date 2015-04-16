package delfos.rs.recommendation;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;

/**
 * Encapsula las recomendaciones hechas a un usuario.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 *
 * @version 1.0 22-Mar-2013
 */
public abstract class RecommendationsFactory implements Serializable {

    private static final long serialVersionUID = 654546L;

    enum RecommendationsTarget {

        USER(User.USER_ID_TARGET_PREFIX),
        GROUP(GroupOfUsers.GROUP_ID_TARGET_PREFIX);

        private final String idTargetBeginWith;

        private RecommendationsTarget(String idTargetBeginWith) {
            this.idTargetBeginWith = idTargetBeginWith;
        }

        public static RecommendationsTarget getTargetType(String idTarget) {
            RecommendationsTarget detectedTargetType = null;
            for (RecommendationsTarget recommendationsTarget : RecommendationsTarget.values()) {
                if (idTarget.startsWith(recommendationsTarget.idTargetBeginWith)) {
                    if (detectedTargetType == null) {
                        detectedTargetType = recommendationsTarget;
                    } else {
                        throw new IllegalStateException("getTargetType() method clash! with argument '" + idTarget + "' two target were triggered: '" + detectedTargetType + "' and '" + recommendationsTarget + "'.");
                    }
                }
            }
            if (detectedTargetType == null) {
                throw new IllegalStateException("No recommendation target detected for '" + idTarget + "'");
            }

            return detectedTargetType;
        }
    }

    public static Recommendations createRecommendations(String idTarget, List<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        RecommendationsTarget targetType = RecommendationsTarget.getTargetType(idTarget);

        switch (targetType) {
            case USER:
                return new SingleUserRecommendations(User.parseIdTarget(idTarget), recommendations, recommendationComputationDetails);
            case GROUP:
                return new GroupRecommendations(GroupOfUsers.parseIdTarget(idTarget), recommendations, recommendationComputationDetails);
            default:
                throw new IllegalArgumentException("Unrecognized idTarget '" + idTarget + "', cannot infer target type (" + Arrays.toString(RecommendationsTarget.values()) + ")");
        }
    }

    public static Recommendations createRecommendations(String idTarget, List<Recommendation> recommendations, long timeTaken) {
        return createRecommendations(idTarget, recommendations, new RecommendationComputationDetails().addDetail(RecommendationComputationDetails.DetailField.TimeTaken, timeTaken));
    }

    public static Recommendations createRecommendations(String idTarget, List<Recommendation> recommendations) {
        return createRecommendations(idTarget, recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
    }

    public static SingleUserRecommendations createRecommendations(User user, List<Recommendation> recommendations) {
        return new SingleUserRecommendations(user, recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
    }

    public static GroupRecommendations createRecommendations(GroupOfUsers groupOfUsers, List<Recommendation> recommendations) {
        return new GroupRecommendations(groupOfUsers, recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
    }

    public static Recommendations copyRecommendationsWithNewRanking(Recommendations recommendations, List<Recommendation> recommendationsNewRanking) {
        return createRecommendations(recommendations.getTargetIdentifier(), recommendationsNewRanking, recommendations.getRecommendationComputationDetails());
    }

    private RecommendationsFactory() {

    }

}
