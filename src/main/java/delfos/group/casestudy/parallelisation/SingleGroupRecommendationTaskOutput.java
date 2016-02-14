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
package delfos.group.casestudy.parallelisation;

import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;

/**
 * Stores the output of the calculation of the recommendations with the stream
 * function {@link SingleGroupRecommendationFunction}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
