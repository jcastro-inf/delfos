package delfos.results.evaluationmeasures;

import org.jdom2.Element;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;

/**
 * Cuenta el número de recomendaciones que se calcularon. También es el
 * numerador en la medida de evaluación de cobertura.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @see Coverage
 */
public class NumberOfRecommendations extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        float numberOfRecommendations = 0;
        Element element = new Element(this.getName());
        for (int idUser : testDataset.allUsers()) {
            numberOfRecommendations += recommendationResults.getRecommendationsForUser(idUser).size();
        }
        element.setAttribute("value", Float.toString(numberOfRecommendations));
        return new MeasureResult(this, numberOfRecommendations);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
