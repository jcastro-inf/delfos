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
package delfos.casestudy.parallelisation;

import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.RecommendationsToUser;

/**
 * Stores the output of the calculation of the recommendations with the stream function {@link RecommendationFunction}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RecommendationTaskOutput {

    private final User user;
    private final RecommendationsToUser recommendations;
    private final long recommendationTime;

    public RecommendationTaskOutput(User user, RecommendationsToUser recommendations, long recommendationTime) {
        this.user = user;
        this.recommendations = recommendations;
        this.recommendationTime = recommendationTime;
    }

    public RecommendationTaskOutput(User user, RecommendationsToUser recommendations) {
        this(user, recommendations, -1);
    }

    public User getUser() {
        return user;
    }

    public long getRecommendationTime() {
        return recommendationTime;
    }

    public RecommendationsToUser getRecommendations() {
        return recommendations;
    }

}
