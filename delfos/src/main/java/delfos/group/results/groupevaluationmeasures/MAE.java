package delfos.group.results.groupevaluationmeasures;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import org.jdom2.Element;

/**
 * Medida de evaluación para calcular el error absoluto medio del sistema de
 * recomendación evaluado. Calcula la diferencia entre la valoración hecha para
 * el grupo y la valoración individual que cada usuario dió para el producto, si
 * lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.RatingPrediction.MAE} para
 * recomendaciones individuales.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (10-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE
 */
public class MAE extends GroupEvaluationMeasure {

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        Element elementMae = new Element(this.getClass().getSimpleName());
        elementMae = ParameterOwnerXML.getElement(this);

        MeanIterative maeGeneral = new MeanIterative();

        for (Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {
            GroupOfUsers groupOfUsers = entry.getKey();

            MeanIterative maeGroup = new MeanIterative();
            Map<Integer, MeanIterative> maeMembers = new TreeMap<>();
            for (User member : groupOfUsers.getMembers()) {
                maeMembers.put(member.getId(), new MeanIterative());
            }

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            groupOfUsers.getIdMembers().stream().forEach((idUser) -> {
                try {
                    groupTrueRatings.put(idUser, testDataset.getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            });

            Collection<Recommendation> recommendationsToGroup = entry.getValue();
            for (Recommendation r : recommendationsToGroup) {
                int idItem = r.getIdItem();
                for (int idUser : groupOfUsers.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predicted = r.getPreference().doubleValue();
                        double absoluteError = Math.abs(predicted - trueRating);

                        maeGeneral.addValue(absoluteError);
                        maeGroup.addValue(absoluteError);
                        maeMembers.get(idUser).addValue(absoluteError);
                    }
                }
            }

            Element groupElement = getGroupElement(groupOfUsers, maeGroup, maeMembers);
            elementMae.addContent(groupElement);

        }

        elementMae.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(maeGeneral.getMean()));
        elementMae.setAttribute("numPredicted",
                Long.toString(maeGeneral.getNumValues()));

        if (maeGeneral.isEmpty()) {
            return new GroupMeasureResult(this, Double.NaN, elementMae);
        } else {
            double mae = maeGeneral.getMean();
            return new GroupMeasureResult(this, mae, elementMae);
        }
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }

    private Element getGroupElement(GroupOfUsers groupOfUsers, MeanIterative maeGroup, Map<Integer, MeanIterative> maeMembers) {
        Element groupElement = new Element(this.getClass().getSimpleName() + "_group");

        groupElement.setAttribute("groupMembers", groupOfUsers.toString());
        groupElement.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(maeGroup.getMean()));
        groupElement.setAttribute("numPredicted",
                Long.toString(maeGroup.getNumValues()));

        for (User member : groupOfUsers.getMembers()) {
            Element memberElement = new Element(this.getClass().getSimpleName() + "_member");

            memberElement.setAttribute(
                    "idMember",
                    Integer.toString(member.getId()));

            memberElement.setAttribute(
                    EvaluationMeasure.VALUE_ATTRIBUTE_NAME,
                    Double.toString(maeMembers.get(member.getId()).getMean()));

            memberElement.setAttribute("numPredicted",
                    Long.toString(maeMembers.get(member.getId()).getNumValues()));

            groupElement.addContent(memberElement);

        }

        return groupElement;
    }
}
