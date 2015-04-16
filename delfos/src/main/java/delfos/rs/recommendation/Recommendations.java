package delfos.rs.recommendation;

import java.io.Serializable;
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
public class Recommendations implements Serializable {

    private static final long serialVersionUID = 654546L;

    private final String targetIdentifier;
    private final List<Recommendation> recommendations;
    private final RecommendationComputationDetails recommendationComputationDetails;

    protected Recommendations() {
        this.targetIdentifier = null;
        this.recommendations = null;
        this.recommendationComputationDetails = null;
    }

    public Recommendations(String targetIdentifier, List<Recommendation> recommendations) {
        this.targetIdentifier = targetIdentifier;
        this.recommendations = new LinkedList<>(recommendations);
        recommendationComputationDetails = new RecommendationComputationDetails();
    }

    public Recommendations(String targetIdentifier, List<Recommendation> recommendations, RecommendationComputationDetails recommendationComputationDetails) {
        this.targetIdentifier = targetIdentifier;
        this.recommendations = new LinkedList<>(recommendations);
        this.recommendationComputationDetails = recommendationComputationDetails;
    }

    public Object getDetails(RecommendationComputationDetails.DetailField detailField) {
        return recommendationComputationDetails.getDetailFieldValue(detailField);
    }

    public String getTargetIdentifier() {
        return targetIdentifier;
    }

    public List<Recommendation> getRecommendations() {
        return new LinkedList<>(recommendations);
    }

    public RecommendationComputationDetails getRecommendationComputationDetails() {
        return recommendationComputationDetails;
    }

    public Set<RecommendationComputationDetails.DetailField> detailFieldSet() {
        return recommendationComputationDetails.detailFieldSet();
    }

}
