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
package delfos.rs.recommendation;

import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;

/**
 * Encapsula las recomendaciones hechas a un usuario.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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

    public static Recommendations createRecommendations(String idTarget, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        RecommendationsTarget targetType = RecommendationsTarget.getTargetType(idTarget);

        switch (targetType) {
            case USER:
                return new RecommendationsToUser(User.parseIdTarget(idTarget), recommendations, recommendationComputationDetails);
            case GROUP:
                return new GroupRecommendations(GroupOfUsers.parseIdTarget(idTarget), recommendations, recommendationComputationDetails);
            default:
                throw new IllegalArgumentException("Unrecognized idTarget '" + idTarget + "', cannot infer target type (" + Arrays.toString(RecommendationsTarget.values()) + ")");
        }
    }

    public static Recommendations createRecommendations(String idTarget, Collection<Recommendation> recommendations, long timeTaken) {
        return createRecommendations(idTarget, recommendations, new RecommendationComputationDetails().addDetail(DetailField.TimeTaken, timeTaken));
    }

    public static Recommendations createRecommendations(String idTarget, Collection<Recommendation> recommendations) {
        return createRecommendations(idTarget, recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
    }

    public static RecommendationsToUser createRecommendations(User user, Collection<Recommendation> recommendations) {
        return new RecommendationsToUser(user, recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
    }

    public static GroupRecommendations createRecommendations(GroupOfUsers groupOfUsers, Collection<Recommendation> recommendations) {
        return new GroupRecommendations(groupOfUsers, recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
    }

    public static Recommendations copyRecommendationsWithNewRanking(Recommendations recommendations, Collection<Recommendation> recommendationsNewRanking) {
        return createRecommendations(recommendations.getTargetIdentifier(), recommendationsNewRanking, recommendations.getRecommendationComputationDetails());
    }

    private RecommendationsFactory() {

    }

}
