package delfos.rs.recommendation;

import java.util.List;
import delfos.dataset.basic.user.User;

/**
 * Encapsula las recomendaciones hechas a un usuario.
 *
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
 *
 * @version 1.0 22-Mar-2013
 */
public class SingleUserRecommendations extends Recommendations {

    private static final long serialVersionUID = 3422345l;

    private final User targetUser;

    private SingleUserRecommendations() {
        super();
        targetUser = null;
    }

    public SingleUserRecommendations(User user, List<Recommendation> recommendations) {
        super(user.getTargetId(), recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
        this.targetUser = user;
    }

    public SingleUserRecommendations(User user, List<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        super(user.getTargetId(), recommendations, recommendationComputationDetails);
        this.targetUser = user;
    }

    public User getTargetUser() {
        return targetUser;
    }
}
