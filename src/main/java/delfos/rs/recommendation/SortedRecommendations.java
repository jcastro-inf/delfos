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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Encapsula las recomendaciones hechas a un usuario.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 22-Mar-2013
 */
public class SortedRecommendations implements Serializable {

    private static final long serialVersionUID = 654546L;

    private final Object target;
    private final List<Recommendation> sortedRecommendations;
    private final RecommendationComputationDetails recommendationComputationDetails;

    private SortedRecommendations() {
        this.target = null;
        this.sortedRecommendations = null;
        this.recommendationComputationDetails = null;
    }

    public SortedRecommendations(Object target, Collection<Recommendation> recommendations) {
        this.target = target;
        this.sortedRecommendations = new LinkedList<>(recommendations);
        Collections.sort(sortedRecommendations);
        recommendationComputationDetails = new RecommendationComputationDetails();
    }

    public SortedRecommendations(Object target, Collection<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        this.target = target;
        this.sortedRecommendations = new LinkedList<>(recommendations);
        Collections.sort(sortedRecommendations);
        this.recommendationComputationDetails = recommendationComputationDetails;
    }

    public Object getDetails(RecommendationComputationDetails.DetailField detailField) {
        return recommendationComputationDetails.getDetailFieldValue(detailField);
    }

    public String getTargetIdentifier() {
        return target.toString();
    }

    public Object getTarget() {
        return target;
    }

    public List<Recommendation> getRecommendations() {
        return new LinkedList<>(sortedRecommendations);
    }

    public RecommendationComputationDetails getRecommendationComputationDetails() {
        return recommendationComputationDetails;
    }

    public Set<RecommendationComputationDetails.DetailField> detailFieldSet() {
        return recommendationComputationDetails.detailFieldSet();
    }

    public SortedRecommendations sortByPreference() {
        return this;
    }

}
