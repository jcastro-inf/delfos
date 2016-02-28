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
package delfos.results.evaluationmeasures.roccurve;

import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;

/**
 * Medida de evaluación para calcular el area bajo roc, tomando el tamaño de la
 * lista de recomendaciones como el umbral. Esta medida calcula la sensitividad
 * y especificidad en cada valor del umbral para generar una curva, cuyo área es
 * uno si el clasificador es perfecto y 0,5 si el clasificador es aleatorio.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class AreaUnderROC extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto de la medida de evaluación.
     */
    public AreaUnderROC() {
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        List<ConfusionMatrix> matrices = new LinkedList<>();
        int maxLength = 0;

        for (Integer idUser : recommendationResults.usersWithRecommendations()) {
            Collection<Recommendation> recommendations
                    = recommendationResults.getRecommendationsForUser(idUser);

            if (recommendations.size() > maxLength) {
                maxLength = recommendations.size();
            }
        }

        if (maxLength == 0) {
            return new MeasureResult(this, 0);
        }

        List<List<Boolean>> resultados = new ArrayList<>(recommendationResults.usersWithRecommendations().size());
        for (int idUser : testDataset.allUsers()) {
            Collection<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            List<Boolean> listaTransformada = new ArrayList<>(recommendationList.size());

            try {
                Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
                recommendationList.stream().map((r) -> r.getIdItem()).map((idItem) -> {
                    return idItem;
                }).forEach((idItem) -> {
                    listaTransformada.add(relevanceCriteria.isRelevant(userRatings.get(idItem).getRatingValue()));
                });
                resultados.add(listaTransformada);
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        Collection<Integer> allUsers = testDataset.allUsers();
        if (allUsers.isEmpty()) {
            throw new IllegalArgumentException("Cannot work without users.");
        }

        int truePositive = 0;
        int falseNegative = 0;
        int falsePositive = 0;
        int trueNegative = 0;
        int count = 0;

        //Inicialmente supone que todos son no recomendados
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("ROC with length " + 0 + " of " + maxLength + ".");
        }
        for (List<Boolean> recom : resultados) {
            for (Boolean recom1 : recom) {
                if (recom1) {
                    falseNegative++;
                } else {
                    trueNegative++;
                }
            }
            count++;
            if (allUsers.size() > 11 && (count % (allUsers.size() / 10.0) == 0)) {
                if (Global.isVerboseAnnoying()) {
                    Global.showInfoMessage(" " + count * 100 / allUsers.size() + "% of users analysed.\n");
                }
            }
        }

        //calculada la matriz de confusión para tamaño 0, se añade a la curva.
        matrices.add(new ConfusionMatrix(falsePositive, falseNegative, truePositive, trueNegative));

        Chronometer c = new Chronometer();
        c.reset();
        for (int kActual = 1; kActual < maxLength; kActual++) {
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("ROC with length " + kActual + " of " + maxLength + ".");
            }

//            count = 0;
            for (List<Boolean> recom : resultados) {
                if (kActual < recom.size()) {
                    if (recom.get(kActual)) {
                        truePositive++;
                        falseNegative--;
                    } else {
                        falsePositive++;
                        trueNegative--;
                    }
                }
            }
            //calculada la matriz de confusión para longitud kActual
            matrices.add(new ConfusionMatrix(falsePositive, falseNegative, truePositive, trueNegative));
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage(" in " + c.printPartialElapsed() + "\n");
            }
        }

        ConfusionMatricesCurve curve = new ConfusionMatricesCurve(matrices.toArray(new ConfusionMatrix[1]));

        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("------------- Receiver Operator Characteristic --------------" + "\n");
            Global.showInfoMessage(curve.toString() + "\n");
        }

        double areaUnderROC = curve.getAreaPRSpace();

        Element element = new Element(this.getName());
        element.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(areaUnderROC));
        element.setContent(ConfusionMatricesCurveXML.getElement(curve));

        return new MeasureResult(
                this,
                areaUnderROC);
    }
}
