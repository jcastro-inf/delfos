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
import java.util.Collection;

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

    public SingleUserRecommendations(User user, Collection<Recommendation> recommendations) {
        super(user.getTargetId(), recommendations, RecommendationComputationDetails.EMPTY_DETAILS);
        this.targetUser = user;
    }

    public SingleUserRecommendations(User user, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        super(user.getTargetId(), recommendations, recommendationComputationDetails);
        this.targetUser = user;
    }

    public User getTargetUser() {
        return targetUser;
    }
}
