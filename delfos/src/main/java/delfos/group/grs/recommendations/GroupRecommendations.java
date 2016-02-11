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
package delfos.group.grs.recommendations;

import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.Recommendations;
import java.util.Collection;

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

    public GroupRecommendations(
            GroupOfUsers groupOfUsers,
            Collection<Recommendation> recommendations,
            RecommendationComputationDetails recommendationComputationDetails) {
        super(groupOfUsers.getTargetId(), recommendations, recommendationComputationDetails);
        this.targetGroupOfUsers = groupOfUsers;
    }

    public GroupRecommendations(
            GroupOfUsers groupOfUsers,
            Collection<Recommendation> recommendations) {
        super(groupOfUsers.getTargetId(), recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
        this.targetGroupOfUsers = groupOfUsers;
    }

    public GroupOfUsers getGroupOfUsers() {
        return targetGroupOfUsers;
    }
}
