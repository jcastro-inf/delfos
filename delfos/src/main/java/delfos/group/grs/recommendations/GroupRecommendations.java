package delfos.group.grs.recommendations;

import java.util.List;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.Recommendations;

/**
 * Encapsula las recomendaciones hechas a un grupo.
 *
 * @author Jorge Castro Gallardo
 */
public class GroupRecommendations extends Recommendations {

    private static final long serialVersionUID = 34235l;

    private final GroupOfUsers targetGroupOfUsers;

    protected GroupRecommendations() {
        super();
        targetGroupOfUsers = null;
    }

    public GroupRecommendations(GroupOfUsers groupOfUsers, List<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        super(groupOfUsers.getTargetId(), recommendations, recommendationComputationDetails);
        this.targetGroupOfUsers = groupOfUsers;
    }

    public GroupOfUsers getGroupOfUsers() {
        return targetGroupOfUsers;
    }
}
