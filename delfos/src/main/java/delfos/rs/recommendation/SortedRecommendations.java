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
 * @author Jorge Castro Gallardo (Sinbad2,Universidad de Jaén)
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
