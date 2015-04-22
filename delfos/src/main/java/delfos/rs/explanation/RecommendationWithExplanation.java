package delfos.rs.explanation;

import delfos.rs.recommendation.Recommendation;
import java.util.Collection;

/**
 *
 * @version 09-sep-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @param <ExplanationType>
 */
public class RecommendationWithExplanation<ExplanationType> {

    private final Collection<Recommendation> recommendations;
    private final ExplanationType explanation;

    public RecommendationWithExplanation(Collection<Recommendation> recommendations, ExplanationType explanation) {
        this.recommendations = recommendations;
        this.explanation = explanation;
    }
}
