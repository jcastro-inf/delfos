package delfos.group.casestudy.parallelisation;

import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;

/**
 * Stores the output of the calculation of the recommendations with the stream
 * function {@link SingleGroupRecommendationFunction}
 *
 * @author Jorge Castro Gallardo
 */
public class SingleGroupRecommendationTaskOutput {

    private final GroupOfUsers group;
    private final Collection<Recommendation> recommendations;
    private final long buildGroupModelTime;
    private final long recommendationTime;

    public SingleGroupRecommendationTaskOutput(GroupOfUsers group, Collection<Recommendation> recommendations, long buildGroupModelTime, long recommendationTime) {
        this.group = group;
        this.recommendations = recommendations;
        this.buildGroupModelTime = buildGroupModelTime;
        this.recommendationTime = recommendationTime;
    }

    public SingleGroupRecommendationTaskOutput(GroupOfUsers group, Collection<Recommendation> recommendations) {
        this(group, recommendations, -1, -1);
    }

    public GroupOfUsers getGroup() {
        return group;
    }

    public long getBuildGroupModelTime() {
        return buildGroupModelTime;
    }

    public long getRecommendationTime() {
        return recommendationTime;
    }

    public Collection<Recommendation> getRecommendations() {
        return Collections.unmodifiableCollection(recommendations);
    }
}
