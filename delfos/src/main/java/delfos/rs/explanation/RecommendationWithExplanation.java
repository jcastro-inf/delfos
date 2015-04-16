package delfos.rs.explanation;

import java.util.List;
import delfos.rs.recommendation.Recommendation;

/**
 *
 * @version 09-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @param <ExplanationType>
 */
public class RecommendationWithExplanation<ExplanationType> {

    private final List<Recommendation> recommendations;
    private final ExplanationType explanation;

    public RecommendationWithExplanation(List<Recommendation> recommendations, ExplanationType explanation) {
        this.recommendations = recommendations;
        this.explanation = explanation;
    }
}
