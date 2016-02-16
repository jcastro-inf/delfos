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
import java.io.Serializable;
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
public class Recommendations implements Serializable {

    private static final long serialVersionUID = 654546L;

    private final Object target;
    private final Collection<Recommendation> recommendations;
    private final RecommendationComputationDetails recommendationComputationDetails;

    protected Recommendations() {
        this.target = null;
        this.recommendations = null;
        this.recommendationComputationDetails = null;
    }

    public Recommendations(Object target, Collection<Recommendation> recommendations) {
        this.target = target;
        this.recommendations = new LinkedList<>(recommendations);
        recommendationComputationDetails = new RecommendationComputationDetails();
    }

    public Recommendations(Object target, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        this.target = target;
        this.recommendations = new LinkedList<>(recommendations);
        this.recommendationComputationDetails = recommendationComputationDetails;
    }

    public Object getDetails(RecommendationComputationDetails.DetailField detailField) {
        return recommendationComputationDetails.getDetailFieldValue(detailField);
    }

    public Object getTarget() {
        return target;
    }

    public String getTargetIdentifier() {
        if (target instanceof User) {
            User user = (User) target;

            return user.getTargetId();
        } else {
            return target.toString();
        }
    }

    public Collection<Recommendation> getRecommendations() {
        return new LinkedList<>(recommendations);
    }

    public RecommendationComputationDetails getRecommendationComputationDetails() {
        return recommendationComputationDetails;
    }

    public Set<RecommendationComputationDetails.DetailField> detailFieldSet() {
        return recommendationComputationDetails.detailFieldSet();
    }

    public SortedRecommendations sortByPreference() {
        return new SortedRecommendations(target, recommendations, recommendationComputationDetails);
    }

}
