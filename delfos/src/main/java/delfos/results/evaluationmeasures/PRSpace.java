package delfos.results.evaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.io.xml.UnrecognizedElementException;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Element;

/**
 * Medida de evaluación que calcula la precisión y recall a lo largo de todos
 * los posibles tamaños de la lista de recomendaciones. Muestra como valor
 * agregado la precisión suponiendo una recomendación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class PRSpace extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

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

                    int idItem = r.getIdItem();
                    resultados.add(relevanceCriteria.isRelevant(userRatings.get(idItem).getRatingValue()));
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

        float areaUnderPR = agregada.getAreaPRSpace();

        Element element = new Element(this.getName());
        element.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Float.toString(areaUnderPR));
        element.setContent(ConfusionMatricesCurveXML.getElement(agregada));

        Map<String, Double> detailedResult = new TreeMap<String, Double>();
        for (int i = 0; i < agregada.size(); i++) {
            double precisionAt = agregada.getPrecisionAt(i);
            detailedResult.put("Precision@" + i, precisionAt);
        }

        return new MeasureResult(
                this,
                areaUnderPR,
                element, detailedResult);
    }

    @Override
    public MeasureResult agregateResults(Collection<MeasureResult> results) {
        List<ConfusionMatricesCurve> curvas = new ArrayList<ConfusionMatricesCurve>(results.size());
        for (MeasureResult mr : results) {
            try {
                Element prCurveElement = mr.getXMLElement();
                ConfusionMatricesCurve c = ConfusionMatricesCurveXML.getConfusionMatricesCurve(prCurveElement);
                curvas.add(c);
            } catch (UnrecognizedElementException ex) {
                ERROR_CODES.UNRECOGNIZED_XML_ELEMENT.exit(ex);
            }
        }

        if (curvas.isEmpty()) {
            Global.showWarning("The curves cannot be reconstructed");
        }

        ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(curvas);

        float areaUnderPR = agregada.getAreaPRSpace();

        Element element = new Element(this.getName());
        element.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Float.toString(areaUnderPR));
        element.setContent(ConfusionMatricesCurveXML.getElement(agregada));

        Map<String, Double> detailedResult = new TreeMap<String, Double>();
        for (int i = 0; i < agregada.size(); i++) {
            double precisionAt = agregada.getPrecisionAt(i);
            detailedResult.put("Precision@" + i, precisionAt);
        }

        return new MeasureResult(
                this,
                areaUnderPR,
                element,
                detailedResult);
    }
}
