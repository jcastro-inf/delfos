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
import delfos.rs.collaborativefiltering.profile.Neighbor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RecommendationsToUserWithNeighbors extends RecommendationsToUser {

    private List<Neighbor> neighbors;

    public static final RecommendationsToUserWithNeighbors EMPTY_LIST = new RecommendationsToUserWithNeighbors(
            User.ANONYMOUS_USER,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST);

    public RecommendationsToUserWithNeighbors() {
        super();
    }

    public RecommendationsToUserWithNeighbors(User user, Collection<Recommendation> recommendations) {
        super(user, recommendations);
    }

    public RecommendationsToUserWithNeighbors(User user, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        super(user, recommendations, recommendationComputationDetails);
    }

    public RecommendationsToUserWithNeighbors(User user, Collection<Recommendation> recommendations, List<Neighbor> neighbors) {
        super(user, recommendations);
        this.neighbors = Collections.unmodifiableList(new ArrayList<Neighbor>(neighbors));
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

}
