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

public class RecommendationsWithNeighbors extends Recommendations {

    private List<Neighbor> neighbors;

    public static final RecommendationsWithNeighbors EMPTY_LIST = new RecommendationsWithNeighbors(
            User.ANONYMOUS_USER.getName(),
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST);

    public RecommendationsWithNeighbors() {
        super();
    }

    public RecommendationsWithNeighbors(String targetIdentifier, Collection<Recommendation> recommendations) {
        super(targetIdentifier, recommendations);
    }

    public RecommendationsWithNeighbors(String targetIdentifier, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        super(targetIdentifier, recommendations, recommendationComputationDetails);
    }

    public RecommendationsWithNeighbors(String targetIdentifier, Collection<Recommendation> recommendations, List<Neighbor> neighbors) {
        super(targetIdentifier, recommendations);
        this.neighbors = Collections.unmodifiableList(new ArrayList<Neighbor>(neighbors));
    }

    public List<Neighbor> getNeighbors() {
        return neighbors;
    }

}
