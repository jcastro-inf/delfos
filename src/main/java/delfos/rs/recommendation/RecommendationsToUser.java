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
import java.util.LinkedList;
import java.util.Set;

/**
 * Encapsula las recomendaciones hechas a un usuario.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 22-Mar-2013
 */
public class RecommendationsToUser extends Recommendations {

    private static final long serialVersionUID = 654546L;

    private final User user;
    private final Collection<Recommendation> recommendations;
    private final RecommendationComputationDetails recommendationComputationDetails;

    protected RecommendationsToUser() {
        this.user = null;
        this.recommendations = null;
        this.recommendationComputationDetails = null;
    }

    public RecommendationsToUser(User user, Collection<Recommendation> recommendations) {
        this.user = user;
        this.recommendations = new LinkedList<>(recommendations);
        recommendationComputationDetails = new RecommendationComputationDetails();
    }

    public RecommendationsToUser(User user, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        this.user = user;
        this.recommendations = new LinkedList<>(recommendations);
        this.recommendationComputationDetails = recommendationComputationDetails;
    }

    @Override
    public Object getDetails(RecommendationComputationDetails.DetailField detailField) {
        return recommendationComputationDetails.getDetailFieldValue(detailField);
    }

    @Override
    public Object getTarget() {
        return user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public String getTargetIdentifier() {
        return user.getTargetId();
    }

    @Override
    public Collection<Recommendation> getRecommendations() {
        return new LinkedList<>(recommendations);
    }

    @Override
    public RecommendationComputationDetails getRecommendationComputationDetails() {
        return recommendationComputationDetails;
    }

    @Override
    public Set<RecommendationComputationDetails.DetailField> detailFieldSet() {
        return recommendationComputationDetails.detailFieldSet();
    }

}
