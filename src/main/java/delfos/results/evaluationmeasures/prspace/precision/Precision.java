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
package delfos.results.evaluationmeasures.prspace.precision;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.item.Item;
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
import org.jdom2.Element;

/**
 * Medida de evaluación que calcula la precisión y recall a lo largo de todos los posibles tamaños de la lista de
 * recomendaciones. Muestra como valor agregado la precisión suponiendo una recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public abstract class Precision extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    private final int listSize;

    public Precision() {
        listSize = 5;
    }

    public Precision(int listSize) {
        this.listSize = listSize;

    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        int maxLength = 0;
        for (int idUser : testDataset.allUsers()) {
            Collection<Recommendation> lr = recommendationResults.getRecommendationsForUser(idUser);

            if (lr.size() > maxLength) {
                maxLength = lr.size();
            }
        }

        Map<Integer, ConfusionMatricesCurve> allUsersCurves = new TreeMap<>();

        for (int idUser : testDataset.allUsers()) {

            List<Boolean> resultados = new ArrayList<>(recommendationResults.usersWithRecommendations().size());
            Collection<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);

            try {
                Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
                for (Recommendation r : recommendationList) {

                    Item item = r.getItem();
                    resultados.add(relevanceCriteria.isRelevant(userRatings.get(item.getId()).getRatingValue()));
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }

            try {
                allUsersCurves.put(idUser, new ConfusionMatricesCurve(resultados));
            } catch (IllegalArgumentException iae) {
                Global.showWarning("User " + idUser + ": " + iae.getMessage());
            }
        }

        ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(allUsersCurves.values());

        double areaUnderPR = agregada.getAreaPRSpace();

        Element element = new Element(this.getName());
        element.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(areaUnderPR));
        element.setContent(ConfusionMatricesCurveXML.getElement(agregada));

        double precisionAt = agregada.getPrecisionAt(listSize);

        return new MeasureResult(
                this,
                precisionAt);
    }
}
