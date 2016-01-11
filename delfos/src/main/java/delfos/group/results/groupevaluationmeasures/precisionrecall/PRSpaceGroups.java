package delfos.group.results.groupevaluationmeasures.precisionrecall;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.io.xml.evaluationmeasures.PRSpaceGroupsXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.UnrecognizedElementException;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Element;

/**
 * Medida de evaluación para sistemas de recomendación a grupos que calcula la
 * precisión y recall a lo largo de todos los tamaños de recomendación al grupo.
 * Usa como test la media de valoraciones de test de los usuarios sobre el
 * producto que se predice.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 15-01-2013
 */
public class PRSpaceGroups extends GroupEvaluationMeasure {

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Map<GroupOfUsers, ConfusionMatricesCurve> prCurves = new TreeMap<>();

        int gruposSinMatriz = 0;
        for (GroupOfUsers group : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(group).getRecommendations();

            List<Boolean> recommendacionesGrupo = new ArrayList<>(groupRecommendations.size());
            for (Recommendation r : groupRecommendations) {
                int idItem = r.getIdItem();

                MeanIterative mean = new MeanIterative();
                for (int idUser : group.getIdMembers()) {
                    try {
                        Map<Integer, ? extends Rating> userRatings = testDataset.getUserRatingsRated(idUser);
                        if (userRatings.containsKey(idItem)) {
                            mean.addValue(testDataset.getUserRatingsRated(idUser).get(idItem).getRatingValue().doubleValue());
                        }
                    } catch (UserNotFound ex) {
                        ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    }
                }
                recommendacionesGrupo.add(relevanceCriteria.isRelevant(mean.getMean()));
            }

            try {
                prCurves.put(group, new ConfusionMatricesCurve(recommendacionesGrupo));
            } catch (IllegalArgumentException iae) {
                gruposSinMatriz++;
            }
        }

        ConfusionMatricesCurve agregada = ConfusionMatricesCurve.mergeCurves(prCurves.values());

        float value;
        if (agregada.size() >= 2) {
            value = agregada.getPrecisionAt(1);
        } else {
            value = Float.NaN;
        }

        if (gruposSinMatriz != 0) {
            Global.showWarning("Grupos sin Matriz en " + PRSpaceGroups.class + " --> " + gruposSinMatriz + " \n");
        }

        Map<String, Double> detailedResult = new TreeMap<>();
        for (int i = 0; i < agregada.size(); i++) {
            double precisionAt = agregada.getPrecisionAt(i);
            detailedResult.put("Precision@" + i, precisionAt);
        }

        return new GroupEvaluationMeasureResult(this, value, PRSpaceGroupsXML.getElement(agregada), detailedResult);
    }

    @Override
    public GroupEvaluationMeasureResult agregateResults(Collection<GroupEvaluationMeasureResult> results) {
        ArrayList<ConfusionMatricesCurve> curves = new ArrayList<>();

        for (GroupEvaluationMeasureResult r : results) {
            Element e = r.getXMLElement();
            e.getChild(PRSpaceGroupsXML.MEASURE_ELEMENT);
            try {
                curves.add(PRSpaceGroupsXML.getConfusionMatricesCurve(e));
            } catch (UnrecognizedElementException ex) {
                ERROR_CODES.UNRECOGNIZED_XML_ELEMENT.exit(ex);
            }
        }

        ConfusionMatricesCurve mergeCurves = ConfusionMatricesCurve.mergeCurves(curves);

        Map<String, Double> detailedResult = new TreeMap<>();
        for (int i = 0; i < mergeCurves.size(); i++) {
            double precisionAt = mergeCurves.getPrecisionAt(i);
            detailedResult.put("Precision@" + i, precisionAt);
        }

        return new GroupEvaluationMeasureResult(
                this,
                mergeCurves.getAreaUnderROC(),
                PRSpaceGroupsXML.getElement(mergeCurves),
                detailedResult);
    }
}
