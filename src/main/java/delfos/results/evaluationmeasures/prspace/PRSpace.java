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
package delfos.results.evaluationmeasures.prspace;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jdom2.Element;

/**
 * Medida de evaluación que calcula la precisión y recall a lo largo de todos los posibles tamaños de la lista de
 * recomendaciones. Muestra como valor agregado la precisión suponiendo una recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PRSpace extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    public static class UsersWithRecommendationsInTestSet implements Predicate<Integer> {

        private final RecommendationResults recommendationResults;
        private final RatingsDataset<? extends Rating> testDataset;

        public UsersWithRecommendationsInTestSet(
                RecommendationResults recommendationResults,
                RatingsDataset<? extends Rating> testDataset) {
            this.recommendationResults = recommendationResults;
            this.testDataset = testDataset;
        }

        @Override
        public boolean test(Integer idUser) {

            Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
            return recommendationResults
                    .getRecommendationsForUser(idUser).parallelStream()
                    .anyMatch(recommendation -> userRatings.containsKey(recommendation.getItem().getId()));
        }

    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        Map<Integer, ConfusionMatricesCurve> allUserCurves = testDataset.allUsers().parallelStream()
                .filter(new UsersWithRecommendationsInTestSet(recommendationResults, testDataset))
                .collect(Collectors.toMap(idUser -> idUser, idUser -> {
                    List<Boolean> resultados = new ArrayList<>();
                    Collection<Recommendation> recommendations = recommendationResults.getRecommendationsForUser(idUser);

                    Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
                    for (Recommendation recommendation : recommendations) {

                        int idItem = recommendation.getItem().getId();
                        if (userRatings.containsKey(idItem)) {
                            resultados.add(relevanceCriteria.isRelevant(userRatings.get(idItem).getRatingValue()));
                        } else {
                            resultados.add(false);
                        }
                    }
                    return new ConfusionMatricesCurve(resultados);
                }));

        ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(allUserCurves.values());

        double areaUnderPR = agregada.getAreaPRSpace();

        Element element = new Element(this.getName());
        element.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(areaUnderPR));
        element.setContent(ConfusionMatricesCurveXML.getElement(agregada));

        Map<String, Double> detailedResult = new TreeMap<>();
        for (int i = 0; i < agregada.size(); i++) {
            double precisionAt = agregada.getPrecisionAt(i);
            detailedResult.put("Precision@" + i, precisionAt);
        }

        return new MeasureResult(
                this,
                areaUnderPR);
    }
}
